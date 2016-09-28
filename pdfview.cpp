#include "pdfview.h"
#include "ui_pdfview.h"

#include <QPicture>
#include <QPainter>
#include <QPdfWriter>
#include <QtPrintSupport/QPrinter>

#include "chord.h"
#include "abstractrenderer.h"

PDFView::PDFView(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::PDFView),
    m_renderer(nullptr)
{
    ui->setupUi(this);
}

PDFView::~PDFView()
{
    delete ui;
    delete m_renderer;
}

void PDFView::setText(const QString &text)
{
    delete m_renderer;
    m_renderer = new AbstractRenderer(QPageSize::size(QPageSize::A4, QPageSize::Millimeter));
    m_renderer->newPage();
    paint(m_renderer, text);
    m_renderer->activatePage(0);
    setPicture();
}

namespace {
const double CHORD_LINE_SPACING = 1.3;
const double LINE_SPACING = 1.0;
static void configurePainter(QPainter* painter)
{
    QFont font = painter->font();
    font.setFamily("lucida");
    painter->setFont(font);
}
}

void PDFView::setPicture()
{
    QPainter p(this);

    if (m_renderer)
    {
        QSize size = m_renderer->currentPage()->sizePainter().toSize();
        QImage image(size, QImage::Format_RGB32);
        QPainter painter(&image);
        painter.fillRect(image.rect(), Qt::white);
        painter.drawPicture(QPoint(), m_renderer->currentPage()->picture());
        painter.end();
        ui->label->setPixmap(QPixmap::fromImage(image));
    }
    else
    {
        ui->label->setPixmap(QPixmap());
    }
}

bool PDFView::pageBreak( const QStringList & lines, const int currentLine, const double heightLeft, const QPainter* painter )
{
    // return whether we must use another page to fit the content
    if (lines[currentLine].isEmpty())
    {
        // we are currently at a paragraph break
        // if next paragraph fits, return false, true otherwise.
        double paragraphHeight = 0;
        double lineHeight =  painter->fontMetrics().height();
        // sum line heights until the next empty line.

        QString paragraph;
        for (int i = currentLine; i < lines.length(); i++ )
        {
            if (i != currentLine && lines[i].isEmpty())
            {
                // paragraph ends
                break;
            }
            QStringList unusedA, unusedB;
            bool isChordLine = Chord::parseLine( lines[i] , unusedA, unusedB );
            if (i != 0)
            {
                if (isChordLine)
                {
                    paragraphHeight += lineHeight * CHORD_LINE_SPACING;
                }
                else
                {
                    paragraphHeight += lineHeight * LINE_SPACING;
                }
            }
            paragraph += lines[i] + "\n";
        }

        return paragraphHeight > heightLeft;
    }
    else
    {
        // we are not at a paragraph break. break if the current line will not fit anymore.
        return painter->fontMetrics().height() > heightLeft;
    }
}


void PDFView::paint(AbstractRenderer *pdfCreator, const QString& text)
{
    QPainter* painter = pdfCreator->currentPage()->painter();
    configurePainter( painter );
    QStringList lines = text.split("\n", QString::KeepEmptyParts);


    double y = pdfCreator->currentPage()->topMargin();
    double height = painter->fontMetrics().height();
    for (int i = 0; i < lines.length(); ++i)
    {
        QString line = lines[i];

        if (pdfCreator->isEndlessPage())
        {
            double spaceLeft = pdfCreator->currentPage()->contentRect().height() - y;

            if (spaceLeft < 0)
            {
                Page* currentPage = pdfCreator->currentPage();
                currentPage->growDownMM( -currentPage->painterUnitsInMM( spaceLeft ) );
            }
        }
        else
        {
            if ( pageBreak( lines,
                            i,
                            pdfCreator->currentPage()->contentRect().bottom() - y,
                            painter                            ))
            {
                AbstractRenderer::drawContinueOnNextPageMark(pdfCreator->currentPage(), painter);
                painter->restore();
                pdfCreator->newPage();
                painter = pdfCreator->currentPage()->painter();
                painter->save();
                configurePainter( painter );
                y = pdfCreator->currentPage()->topMargin();
            }
        }

        QRegExp regexp( Chord::SPLIT_PATTERN );
        QStringList tokens;
        int lastJ = 0;
        int j = 0;
        while ( (j = regexp.indexIn(line, j)) >= 0 )
        {
            int n;
            n = regexp.matchedLength();
            tokens << line.mid(lastJ, j - lastJ);
            tokens << line.mid(j, n);
            lastJ = j + 1;
            j += n;
        }
        tokens << line.mid(lastJ);

        QStringList unusedA, unusedB;
        bool isChordLine = Chord::parseLine( line , unusedA, unusedB );

        int pos = pdfCreator->currentPage()->leftMargin();
        for (const QString & token : tokens)
        {
            painter->save();

            if (isChordLine && Chord(token).isValid())
            {
                QFont font = painter->font();
                font.setBold( true );
                painter->setFont( font );
            }
            int w = painter->fontMetrics().width( token );
            painter->drawText( QRectF( pos, y, pos + w, height ), token );
            pos += w;

            painter->restore();
        }

        if (isChordLine)
        {
            y += height * CHORD_LINE_SPACING;
        }
        else
        {
            y += height * LINE_SPACING;
        }
    }
}

void PDFView::on_buttonNext_clicked()
{
    const int i = m_renderer->currentIndex();
    if (i+1 < m_renderer->pageCount())
    {
        m_renderer->activatePage(i+1);
    }
    setPicture();
}

void PDFView::on_buttonPrevious_clicked()
{
    const int i = m_renderer->currentIndex();
    if (i-1 >= 0)
    {
        m_renderer->activatePage(i-1);
    }
    setPicture();
}

void PDFView::paint(QPagedPaintDevice& ppd)
{
    QPainter painter(&ppd);
    ppd.setPageSize(QPageSize(QPageSize::A4));
    for (int page = 0; page < m_renderer->pageCount(); ++page)
    {
        if (page > 0)
        {
            ppd.newPage();
        }
        m_renderer->activatePage(page);
        painter.drawPicture(0, 0, m_renderer->currentPage()->picture());
    }
}

void PDFView::savePDF(const QString &filename)
{
    QPdfWriter writer(filename);
    paint(writer);
}

void PDFView::print(QPrinter &printer)
{
    paint(printer);
}
