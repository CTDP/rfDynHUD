#include "image_util.h"

unsigned int bitCount( unsigned int i )
{
    // HD, Figure 5-2
    i = i - ( ( i >> 1 ) & 0x55555555 );
    i = ( i & 0x33333333 ) + ( ( i >> 2 ) & 0x33333333 );
    i = ( i + ( i >> 4 ) ) & 0x0f0f0f0f;
    i = i + ( i >> 8 );
    i = i + ( i >> 16 );
    
    return ( i & 0x3f );
}

unsigned int highestOneBit( unsigned int i )
{
    // HD, Figure 3-1
    i |= ( i >>  1 );
    i |= ( i >>  2 );
    i |= ( i >>  4 );
    i |= ( i >>  8 );
    i |= ( i >> 16 );
    
    return ( i - ( i >> 1 ) );
}

unsigned int roundUpPower2( unsigned int v )
{
    switch ( bitCount( v ) )
    {
        case 0:
            return ( 1 );
        case 1:
            return ( v );
        default:
            return ( highestOneBit( v ) << 1 );
    }
}
