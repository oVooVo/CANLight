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

private slots:
    void on_buttonBack_clicked();
    void tableOfContentsError(QNetworkReply::NetworkError networkError);
    void tableOfContentsSslErrors(QList<QSslError> errors);
    void tableOfContentsArrived();

    void contentError(QNetworkReply::NetworkError networkError);
    void contentSslErrors(QList<QSslError> errors);
    void contentArrived();


    void on_listWidget_clicked(const QModelIndex &index);

private:
    Ui::ImportDialog *ui;
    QNetworkAccessManager m_networkAccessManager;
    QNetworkReply* m_tableOfContentsReply;
    QNetworkReply* m_contentReply;
    QProgressDialog m_progressDialog;
    QList<QPair<QString, QUrl>> parseToc(const QByteArray& data);
    QString parseContent(const QByteArray& data);
    QList<QUrl> m_urls;
};

#endif // IMPORTDIALOG_H
