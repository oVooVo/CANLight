#ifndef IMPORTDIALOG_H
#define IMPORTDIALOG_H

#include <QDialog>
#include <QtNetwork/QNetworkAccessManager>
#include <QProgressDialog>
#include <QtNetwork/QNetworkReply>

namespace Ui {
class ImportDialog;
}

class ImportDialog : public QDialog
{
    Q_OBJECT

public:
    explicit ImportDialog(const QString &searchString, QWidget *parent = 0);
    ~ImportDialog();
    QString currentPattern() const;
    void rejectLater();
    int exec();

private slots:
    void on_buttonBack_clicked();
    void on_listWidget_clicked(const QModelIndex &index);

private:
    Ui::ImportDialog *ui;
    QNetworkAccessManager m_networkAccessManager;
    QList<QPair<QString, QUrl>> parseToc(const QByteArray& data);
    QString parseContent(const QByteArray& data);
    QList<QUrl> m_urls;
    bool m_reject = false;
};

#endif // IMPORTDIALOG_H
