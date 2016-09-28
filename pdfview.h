#ifndef PDFVIEW_H
#define PDFVIEW_H

#include <QWidget>
#include <QPicture>

namespace Ui {
class PDFView;
}

class AbstractRenderer;
class QPrinter;
class QPagedPaintDevice;
class PDFView : public QWidget
{
    Q_OBJECT

public:
    explicit PDFView(QWidget *parent = 0);
    ~PDFView();
    void setText(const QString& text);
    void savePDF(const QString& filename);
    void print(QPrinter &printer);

private slots:
    void on_buttonNext_clicked();
    void on_buttonPrevious_clicked();

private:
    void setPicture();
    Ui::PDFView *ui;
    bool pageBreak( const QStringList & lines, const int currentLine, const double heightLeft, const QPainter* painter );
    void paint(AbstractRenderer *pdfCreator, const QString& text);
    void paint(QPagedPaintDevice& ppd);
    AbstractRenderer* m_renderer;

};

#endif // PDFVIEW_H
