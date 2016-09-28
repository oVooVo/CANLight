#include "importdialog.h"
#include "ui_importdialog.h"
#include "global.h"

#include <QtNetwork/QNetworkRequest>
#include <QMessageBox>
#include <QTimer>
#include <QtXml/QtXml>
#include <QtXml/QDomNodeList>

namespace {
static const QString URL_PATTERN = "https://www.ultimate-guitar.com/search.php?search_type=title&order=&value=%1";
}

ImportDialog::ImportDialog(const QString& searchString, QWidget *parent) :
    QDialog(parent),
    ui(new Ui::ImportDialog),
    m_tableOfContentsReply(nullptr),
    m_contentReply(nullptr),
    m_progressDialog(this)
{
    ui->setupUi(this);
    ui->stackedWidget->setCurrentIndex(0);

    const QString url = URL_PATTERN.arg(QString(QUrl::toPercentEncoding(searchString)));
    QNetworkRequest request = QNetworkRequest(QUrl(url));
    m_tableOfContentsReply = m_networkAccessManager.get(request);

    connect(&m_progressDialog, SIGNAL(canceled()), m_tableOfContentsReply, SLOT(abort()));
    connect(m_tableOfContentsReply, SIGNAL(finished()), this, SLOT(tableOfContentsArrived()));
    connect(m_tableOfContentsReply, SIGNAL(error(QNetworkReply::NetworkError)), this, SLOT(tableOfContentsError(QNetworkReply::NetworkError)));
    connect(m_tableOfContentsReply, SIGNAL(sslErrors(QList<QSslError>)), this, SLOT(tableOfContentsSslErrors(QList<QSslError>)));


    m_progressDialog.setWindowTitle(qAppName());
    m_progressDialog.setLabelText(tr("Waiting for table of contents ..."));
    m_progressDialog.setRange(0, 0);
    m_progressDialog.exec();

}

ImportDialog::~ImportDialog()
{
    delete ui;
}

void ImportDialog::on_buttonBack_clicked()
{
    ui->stackedWidget->setCurrentIndex(0);
}

void ImportDialog::tableOfContentsError(QNetworkReply::NetworkError networkError)
{
    qWarning() << "Network Error: " << networkError;
    QMessageBox::critical(this, qAppName(), tr("An error occured: \n%1").arg(m_tableOfContentsReply->errorString()));
    m_tableOfContentsReply->deleteLater();
    m_tableOfContentsReply = nullptr;
    m_progressDialog.close();
    reject();
}

void ImportDialog::tableOfContentsSslErrors(QList<QSslError> errors)
{
    Q_UNUSED(errors);
    {
        QMessageBox::critical(this, qAppName(), tr("SSL errors occured. Abort."));
        m_tableOfContentsReply->deleteLater();
        m_tableOfContentsReply = nullptr;
        m_progressDialog.close();
        reject();
    }
}

void ImportDialog::tableOfContentsArrived()
{
    QByteArray data = m_tableOfContentsReply->readAll();
    m_tableOfContentsReply->deleteLater();
    m_tableOfContentsReply = nullptr;
    m_progressDialog.close();

    if (data.isEmpty())
    {
        qWarning() << "Received no data.";
        QMessageBox::warning(this, qAppName(), tr("Did not receive any data."));
        QTimer::singleShot(1, this, SLOT(reject()));
    }

    Q_ASSERT(m_urls.isEmpty());
    for (const QPair<QString, QUrl>& p : parseToc(data))
    {
        ui->listWidget->addItem(p.first);
        m_urls << p.second;
    }
}

void ImportDialog::contentError(QNetworkReply::NetworkError networkError)
{
    qWarning() << "Network Error: " << networkError;
    QMessageBox::critical(this, qAppName(), tr("An error occured: \n%1").arg(m_contentReply->errorString()));
    m_contentReply->deleteLater();
    m_contentReply = nullptr;
    m_progressDialog.close();
}

void ImportDialog::contentSslErrors(QList<QSslError> errors)
{
//    if (m_tableOfContentsReply->ignoreSslErrors(errors))
//    {
//        qWarning() << "non-fatal SSL errors occured.";
//    }
//    else
    {
        QMessageBox::critical(this, qAppName(), tr("SSL errors occured. Abort."));
        m_contentReply->deleteLater();
        m_contentReply = nullptr;
        m_progressDialog.close();
    }
}

void ImportDialog::contentArrived()
{
    QByteArray data = m_contentReply->readAll();
    m_contentReply->deleteLater();
    m_contentReply = nullptr;
    m_progressDialog.close();

    if (data.isEmpty())
    {
        qWarning() << "Received no data.";
        QMessageBox::warning(this, qAppName(), tr("Did not receive any data."));
        return;
    }

    ui->chordPatternEdit->setChordPattern(parseContent(data));

    ui->stackedWidget->setCurrentIndex(1);
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
    Q_ASSERT(m_contentReply == nullptr);
    const QUrl url = m_urls[index.row()];
    qDebug() << "Url = " << url;
    QNetworkRequest request = QNetworkRequest(url);
    m_contentReply = m_networkAccessManager.get(request);

    connect(&m_progressDialog, SIGNAL(canceled()), m_contentReply, SLOT(abort()));
    connect(m_contentReply, SIGNAL(finished()), this, SLOT(contentArrived()));
    connect(m_contentReply, SIGNAL(error(QNetworkReply::NetworkError)), this, SLOT(contentError(QNetworkReply::NetworkError)));
    connect(m_contentReply, SIGNAL(sslErrors(QList<QSslError>)), this, SLOT(contentSslErrors(QList<QSslError>)));

    m_progressDialog.setWindowTitle(qAppName());
    m_progressDialog.setLabelText(tr("Waiting for contents ..."));
    m_progressDialog.setRange(0, 0);
    m_progressDialog.exec();
}

QString ImportDialog::currentPattern() const
{
    return ui->chordPatternEdit->toPlainText();
}
