#include "abstractrenderer.h"

#include <QFileDialog>
#include <QMessageBox>
#include <QProgressDialog>
#include <qmath.h>

#include "global.h"
#include "util.h"
#include "chord.h"

const QString AbstractRenderer::HYPHEN = QChar(0x2014);
const int AbstractRenderer::ALIGN_SONGS__SEPARATE_PAGES = 5;

AbstractRenderer::AbstractRenderer(QSizeF baseSizeMM) :
    m_baseSizeMM( baseSizeMM ),
    m_currentIndex(-1)
{
}

AbstractRenderer::~AbstractRenderer()
{
    qDeleteAll( m_pages );
    m_pages.clear();
}

void AbstractRenderer::newPage()
{
    const int index = m_pages.length();
    Page* page = new Page(m_baseSizeMM);
    page->setAdditionalTopMargin(0);
    m_pages.insert(index, page);
    activatePage(index);
}

void AbstractRenderer::activatePage( int i )
{
    Q_ASSERT( i >= 0 && i < m_pages.length() );
    m_currentIndex = i;
}

int AbstractRenderer::currentIndex() const
{
    return m_currentIndex;
}

Page* AbstractRenderer::currentPage() const
{
    return m_pages[ m_currentIndex ];
}

bool AbstractRenderer::isEndlessPage() const
{
    return false;
}


//////////////////////////////////////////////
////
///  pdf paint member
//
/////////////////////////////////////////////

void AbstractRenderer::paintHeadline(Page* page, const QString& label)
{
    QPainter* painter = page->painter();
    painter->save();

    QFont font = painter->font();
    font.setBold( true );
    font.setPointSizeF( 15 );
    font.setFamily( "lucida" );
    painter->setFont( font );

    double fontHeight = painter->fontMetrics().height();
    painter->drawText( QPointF(page->leftMargin(), page->topMargin() + fontHeight), label);
    page->setAdditionalTopMargin(2*fontHeight);
    painter->restore();
}

void AbstractRenderer::drawContinueOnNextPageMark(const Page* page, QPainter *painter)
{
    painter->save();
    QFont font = painter->font();
    font.setBold( true );
    font.setPointSizeF( font.pointSizeF() * 3 );
    painter->setFont( font );
    QTextOption option;
    option.setAlignment( Qt::AlignBottom | Qt::AlignRight );
    painter->drawText( page->contentRect(), QChar(0x293E), option );
    painter->restore();
}

QString labelTableOfContents()
{
    return QObject::tr("Setlist");
}

void AbstractRenderer::decoratePageNumbers()
{
    for (int i = 0; i < m_pages.length(); ++i)
    {
        activatePage( i );
        QPainter* painter = currentPage()->painter();
        double height = painter->fontMetrics().height();

        double y = currentPage()->rect().height() - currentPage()->bottomMargin();
        painter->drawText( QRectF( 0, y - height/2, currentPage()->rect().width(), height ),
                                   QString("%1").arg( i + 1 ),
                                   QTextOption( Qt::AlignCenter )                       );
    }
}

int AbstractRenderer::pageCount() const
{
    return m_pages.length();
}
