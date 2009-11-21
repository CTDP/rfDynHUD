This folder contains images, that can be accessed in the Widget code.

Use the TextureLoader to load these images into renderable textures.

If an image is called "image1.png" and resides directly in this folder, load it through:
TextureImage2D texture = TextureLoader.getTexture( "image1.png" );

If an image resides in a subfolder called "my_sepcial_images" and the image itself is called "image2.png", load it through:
TextureImage2D texture = TextureLoader.getTexture( "my_special_images/image2.png" );
