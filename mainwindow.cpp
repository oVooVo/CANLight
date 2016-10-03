#include "mainwindow.h"
#include "ui_mainwindow.h"

#include <QFileDialog>
#include <QtPrintSupport/QPrinter>
#include <QtPrintSupport/QPrintDialog>
#include <QJsonObject>
#include <QJsonDocument>
#include <QSettings>
#include <QJsonArray>
#include <QTimer>

#include "global.h"
#include "importdialog.h"

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow),
    m_currentRow(-1)
{
    ui->setupUi(this);
    ui->stackedWidget->setCurrentIndex(0);
    connect(ui->listWidget, SIGNAL(newItemRequest()), this, SLOT(addItem()));

    load();

}

MainWindow::~MainWindow()
{
    save();
    delete ui;
}

void MainWindow::on_pushButtonBackToList_clicked()
{
    Q_ASSERT(m_currentRow >= 0);
    ui->stackedWidget->setCurrentIndex(0);
    m_patterns[m_currentRow] = ui->chordPatternEdit->toPlainText();

    m_currentRow = -1;
}

void MainWindow::on_buttonTransposeDown_clicked()
{
    ui->chordPatternEdit->transpose(-1);
}

void MainWindow::on_buttonTransposeUp_clicked()
{
    ui->chordPatternEdit->transpose(1);
}

void MainWindow::on_buttonPDF_clicked()
{
    ui->pdfView->setText(ui->chordPatternEdit->toPlainText());
    ui->stackedWidget->setCurrentIndex(2);
}

void MainWindow::addItem(const QString& text, const QString& pattern)
{
    Q_ASSERT(ui->listWidget->count() == m_patterns.length());

    const int row = ui->listWidget->model()->rowCount();
    ui->listWidget->addItem(text);
    const QModelIndex index = ui->listWidget->model()->index(row, 0);
    QListWidgetItem* const item = ui->listWidget->item(row);
    item->setFlags(Qt::ItemIsEnabled | Qt::ItemIsEditable | Qt::ItemIsSelectable);
    if (text.isEmpty())
    {
        ui->listWidget->edit(index);
    }
    m_patterns.append(pattern);

}

void MainWindow::on_listWidget_clicked(const QModelIndex &index)
{
    Q_ASSERT(m_currentRow = -1);
    m_currentRow = index.row();
    const QString pattern = m_patterns[m_currentRow];
    ui->chordPatternEdit->setChordPattern(pattern);

    ui->stackedWidget->setCurrentIndex(1);
}


void MainWindow::on_pushButtonBackToEdit_clicked()
{
    ui->stackedWidget->setCurrentIndex(1);
}

void MainWindow::on_pushButtonSavePdf_clicked()
{
    QString filename = QDir::homePath();
    filename = QFileDialog::getSaveFileName(this, qAppName(), filename);
    if (!filename.isEmpty())
    {
        ui->pdfView->savePDF(filename);
    }
}

void MainWindow::on_pushButtonPrint_clicked()
{
    QPrinter printer;
    QPrintDialog pd(&printer, this);
    if (pd.exec() == QDialog::Accepted)
    {
        ui->pdfView->print(printer);
    }
}

void MainWindow::fromJson(const QJsonDocument &json)
{
    m_patterns.clear();
    ui->listWidget->clear();

    const QJsonArray array = json.array();
    for (const QJsonValue& val : array)
    {
        const QJsonObject object = val.toObject();
        const QString name = object["Name"].toString();
        const QString pattern = object["Pattern"].toString();
        addItem(name, pattern);
    }
}

QJsonDocument MainWindow::toJson() const
{
    QJsonArray array;
    for (int row = 0; row < ui->listWidget->count(); ++row)
    {
        QJsonObject object;
        const QString name = ui->listWidget->item(row)->text();
        QString pattern = m_patterns[row];
        if (row == m_currentRow)
        {
            pattern = ui->chordPatternEdit->toPlainText();
        }
        object.insert("Name", name);
        object.insert("Pattern", pattern);
        array.append(object);
    }

    return QJsonDocument(array);
}

void MainWindow::save()
{
    QSettings settings;
    const QString data = toJson().toJson();
    settings.setValue("Data", data);
}

void MainWindow::load()
{
    QSettings settings;
    const QByteArray data = settings.value("Data").toByteArray();
    fromJson(QJsonDocument::fromJson(data));
}

void MainWindow::exportToFile()
{

}

void MainWindow::importFromFile()
{

}

void MainWindow::on_buttonImport_clicked()
{
    Q_ASSERT(m_currentRow >= 0);
    const QString searchString = ui->listWidget->item(m_currentRow)->text();

    ImportDialog dialog(searchString, this);
    if (dialog.exec() == QDialog::Accepted)
    {
        ui->chordPatternEdit->setChordPattern(dialog.currentPattern());
    }
}
