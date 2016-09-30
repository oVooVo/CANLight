#include "abstractnetworkreceiver.h"

#include <QProgressDialog>
#include <QMessageBox>
#include <QApplication>
#include <QTimer>

#include "importdialog.h"

AbstractNetworkReceiver::AbstractNetworkReceiver(QNetworkReply &reply, ImportDialog &parentDialog, std::function<void(const QByteArray data)> finishedCallback) :
    QObject(nullptr),
    m_reply(reply),
    m_parentDialog(parentDialog),
    m_finishedCallback(finishedCallback)
{
    connect(&reply, SIGNAL(finished()), this, SLOT(onFinished()));
    connect(&reply, SIGNAL(error(QNetworkReply::NetworkError)), this, SLOT(onError(QNetworkReply::NetworkError)));
    connect(&reply, SIGNAL(sslErrors(QList<QSslError>)), this, SLOT(onSslErrors(QList<QSslError>)));

    connect(&m_progressDialog, SIGNAL(canceled()), &reply, SLOT(abort()));
    m_progressDialog.setRange(0, 0);
    m_progressDialog.exec();
}

void AbstractNetworkReceiver::onFinished()
{
    const QByteArray data = m_reply.readAll();
    if (data.isEmpty())
    {
        if (m_reply.error() == QNetworkReply::NoError)
        {
            qWarning() << "Received no data.";
            QMessageBox::warning(&m_parentDialog, qAppName(), tr("Did not receive any data."));
        }
    }
    else
    {
        m_finishedCallback(data);
    }

    m_progressDialog.close();
    m_reply.deleteLater();
    deleteLater();
}

void AbstractNetworkReceiver::onError(QNetworkReply::NetworkError networkError)
{
    Q_UNUSED(networkError);
    m_parentDialog.rejectLater();
    QMessageBox::critical(&m_parentDialog, qAppName(), tr("An error occured: \n%1").arg(m_reply.errorString()));
}

void AbstractNetworkReceiver::onSslErrors(QList<QSslError> errors)
{
    Q_UNUSED(errors);
}

