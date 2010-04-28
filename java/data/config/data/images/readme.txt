This folder contains image files, that can be accessed in the Widget code.

There are two ways to load an image.

1. Directly through the TextureLoader.
ImageTemplate it = TextureLoader.getImage( "image1.png" );
TextureImage2D texture = it.getTextureImage();
or
TextureImage2D texture = it.getScaledTextureImage( width, height );

2. Managed by an ImageProperty
First define the property in your Widget class.
private final ImageProperty imgProp = new ImageProperty( this, "imgProp", "image2.png" );

Or if you want the property to automatically manage a TextureImage field, do it like this.
private TextureImage2D texture = null;
private final ImageProperty imgProp = new ImageProperty( this, "imgProp", "image2.png" )
{
    @Override
    public void setValue( Object value )
    {
        super.setValue( value );
        
        texture = null;
    }
}
ImageTemplate it = imgProp.getImage();

And the proceed like in 1.

If an image resides in a subfolder called "my_sepcial_images" and the image itself is called "image3.png", load it through:
ImageTemplate it = TextureLoader.getImage( "my_special_images/image3.png" );
