#ifndef ABSTRACTNETWORKRECEIVER_H
#define ABSTRACTNETWORKRECEIVER_H

#include <QObject>
#include <functional>
#include <QNetworkReply>
#include <QProgressDialog>
class ImportDialog;
class AbstractNetworkReceiver : public QObject
{
    Q_OBJECT
public:
    explicit AbstractNetworkReceiver(QNetworkReply& reply, ImportDialog& parentDialog, std::function<void(const QByteArray data)> finishedCallback);

private slots:
    void onError(QNetworkReply::NetworkError networkError);
    void onSslErrors(QList<QSslError> errors);
    void onFinished();

private:
    QNetworkReply& m_reply;
    ImportDialog& m_parentDialog;
    QProgressDialog m_progressDialog;
    const std::function<void(const QByteArray data)> m_finishedCallback;
};

#endif // ABSTRACTNETWORKRECEIVER_H
