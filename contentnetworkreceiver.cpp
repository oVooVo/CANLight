//#include "contentnetworkreceiver.h"

//#include <QMessageBox>
//#include <QtNetwork/QNetworkReply>

//#include "util.h"


//ContentNetworkReceiver::ContentNetworkReceiver(QNetworkReply& reply, QDialog &parentDialog) :
//    AbstractNetworkReceiver(reply, parentDialog, progressDialog),
//    m_parentDialog(parentDialog)
//{

//}

//void ContentNetworkReceiver::onError(QNetworkReply::NetworkError networkError)
//{
//    qWarning() << "Network Error: " << networkError;
//    QMessageBox::critical(
//                qobject_assert_cast<QWidget*>(parent()),
//                qAppName(),
//                tr("An error occured: \n%1").arg(
//                    m_tableOfContentsReply->errorString())
//                );
//    m_parentDialog.reject();
//    AbstractNetworkReceiver::onError(networkError);
//}

//void ContentNetworkReceiver::onSslErrors(QList<QSslError> errors)
//{

//}

