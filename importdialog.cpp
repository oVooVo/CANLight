#include "importdialog.h"
#include "ui_importdialog.h"
#include "global.h"

#include <QtNetwork/QNetworkRequest>
#include <QMessageBox>
#include <QTimer>
#include <QtXml/QtXml>
#include <QtXml/QDomNodeList>
#include <functional>
#include "abstractnetworkreceiver.h"

namespace {
static const QString URL_PATTERN = "https://www.ultimate-guitar.com/search.php?search_type=title&order=&value=%1";
}

ImportDialog::ImportDialog(const QString& searchString, QWidget *parent) :
    QDialog(parent),
    ui(new Ui::ImportDialog)
{
    ui->setupUi(this);
    ui->stackedWidget->setCurrentIndex(0);

    auto tocFinishedCallback = [this](const QByteArray& data)
    {
        Q_ASSERT(m_urls.isEmpty());
        for (const QPair<QString, QUrl>& p : parseToc(data))
        {
            ui->listWidget->addItem(p.first);
            m_urls << p.second;
        }
    };

    const QString url = URL_PATTERN.arg(QString(QUrl::toPercentEncoding(searchString)));
    QNetworkRequest request = QNetworkRequest(QUrl(url));
    QNetworkReply* reply = m_networkAccessManager.get(request);
    new AbstractNetworkReceiver(*reply, *this, tocFinishedCallback); // deletes itself.
}

ImportDialog::~ImportDialog()
{
    delete ui;
}

void ImportDialog::on_buttonBack_clicked()
{
    ui->stackedWidget->setCurrentIndex(0);
}

QString ImportDialog::parseContent(const QByteArray& data)
{
    const QString startSequence = "<pre class=\"js-tab-content\">";
    const QString endSequence = "</pre>";
    const int startIndex = data.indexOf(startSequence) + startSequence.length();
    const int endIndex = data.indexOf(endSequence, startIndex + 1);
    QString pattern = data.mid(startIndex, endIndex - startIndex);
    pattern = pattern.replace("<span>", "");
    pattern = pattern.replace("</span>", "");
    return pattern;
}

QList<QPair<QString, QUrl> > ImportDialog::parseToc(const QByteArray &data)
{
    QString text = data;
    int i = text.indexOf("<div class=\"content\">");
    if (i < 0)
    {
        QMessageBox::information(this, qAppName(), tr("No results."));
        QTimer::singleShot(1, this, SLOT(reject()));
    }
    else
    {
        text = text.mid(i);
    }

    QString pattern = QRegularExpression::escape("<span>[ <b class=\"ratdig\">")
                    + "[0-9]*"
                    + QRegularExpression::escape("</b> ]</span></td>")
                    + "\\s*"
                    + QRegularExpression::escape("<td><strong>")
                    + "(chords|tab)"
                    + QRegularExpression::escape("</strong></td>");
    QRegularExpression expr = QRegularExpression(pattern);
    QList<int> indices;
    indices << -1;
    while (true)
    {
        QRegularExpressionMatch match = expr.match(text, indices.last() + 1);
        if (match.hasMatch())
        {
            indices << match.capturedEnd();
        }
        else
        {
            break;
        }
    }
    indices[0] = 0;

    QStringList entries;
    for (int i = 1; i < indices.length(); ++i)
    {
        const int length = indices[i] - indices[i-1];
        entries << text.mid(indices[i-1], length);
    }

    const QRegularExpression chordTabExpr(    QRegularExpression::escape("<td><strong>")
                                            + "(tab|chords)"
                                            + QRegularExpression::escape("</strong></td>"));
    const QRegularExpression urlExpr( QRegularExpression::escape("<a onclick=")
                                      + ".*"
                                      + QRegularExpression::escape("href=")
                                      + ".*"
                                      + QRegularExpression::escape("class=\"song result-link\">") );
    const QRegularExpression endName(  QRegularExpression::escape("</div>")
                                     + "|"
                                     + QRegularExpression::escape("<div")
                                     + ".*"
                                     + QRegularExpression::escape(">") );
    QList<QPair<QString, QUrl>> list;
    for (const QString& entry : entries)
    {
        const QRegularExpressionMatch typeMatch = chordTabExpr.match(entry);
        const QString type = typeMatch.captured().contains("tab") ? "Tab" : "Chords";

        const QString urlStartSequence = "https://";
        const QString urlEndSequence = "\" class";
        const int urlStart = entry.indexOf(urlStartSequence);
        const int urlEnd = entry.indexOf(urlEndSequence, urlStart + 1);
        const QString url = entry.mid(urlStart, urlEnd - urlStart);

        const QString nameStartSequence = "class=\"song result-link\">";
        const int nameStart = entry.indexOf(nameStartSequence) + nameStartSequence.length();
        const int nameEnd = entry.indexOf(endName, nameStart + 1);
        QString name = entry.mid(nameStart, nameEnd - nameStart);
        QTextDocument doc;
        doc.setHtml(name);
        name = doc.toPlainText();
        name.trimmed();
        name.remove(QRegularExpression("[\n\r\t]"));
        list << qMakePair(QString("%1: %2").arg(type, name), QUrl(url));
    }

    return list;
}

void ImportDialog::on_listWidget_clicked(const QModelIndex &index)
{
    const QUrl url = m_urls[index.row()];
    qDebug() << "Url = " << url;

    auto contentFinishedCallback = [this](const QByteArray& data)
    {
        ui->chordPatternEdit->setChordPattern(parseContent(data));
        ui->stackedWidget->setCurrentIndex(1);
    };

    QNetworkRequest request = QNetworkRequest(url);
    QNetworkReply* reply = m_networkAccessManager.get(request);
    new AbstractNetworkReceiver(*reply, *this, contentFinishedCallback); // deletes itself.
}

QString ImportDialog::currentPattern() const
{
    return ui->chordPatternEdit->toPlainText();
}

void ImportDialog::rejectLater()
{
    m_reject = true;
    QTimer::singleShot(1, this, SLOT(reject()));
}

int ImportDialog::exec()
{
    if (m_reject)
    {
        return QDialog::Rejected;
    }
    else
    {
        return QDialog::exec();
    }
}
