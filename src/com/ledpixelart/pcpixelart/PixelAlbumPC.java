package com.ledpixelart.pcpixelart;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.RgbLedMatrix;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.spi.Log;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.pc.IOIOSwingApp;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class PixelAlbumPC extends IOIOSwingApp implements ActionListener {
	private static final String BUTTON_PRESSED = "bp";
	private static RgbLedMatrix matrix_;
	private static RgbLedMatrix.Matrix KIND;  //have to do it this way because there is a matrix library conflict
	private static final String LOG_TAG = "PixelTest";	  	
  	private static short[] frame_;
  	private static byte[] BitmapBytes;
  	private byte[] BitmayArray;
  	private static InputStream BitmapInputStream;
  	private static String ImageName;
  	private static int width_original;
  	private static int height_original; 	  
  	private static float scaleWidth; 
  	private static float scaleHeight; 	  
  	private static final String TAG = "PixelAlbumPC";
  	private static BufferedImage originalImage;
	private static BufferedImage RGB565Image;
	private static BufferedImage ResizedImage;

	
	public static void main(String[] args) throws Exception {  //this used to be static
		new PixelAlbumPC().go(args); //ui stuff 
		
		KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; 	
	    frame_ = new short [KIND.width * KIND.height];
		BitmapBytes = new byte[KIND.width * KIND.height *2]; //512 * 2 = 1024 or 1024 * 2 = 2048
		
		//************ this part of code writes to the LED matrix in code without any external file *********
	    //  writeTest(); //this just writes a test pattern to the LEDs in code without using any external file, uncomment out this line if you want to see that and then comment out the next two lines
	    //***************************************************************************************************
		
		///************ this set of code loads a raw RGB565 image *****************
	    // BitmapInputStream = PixelAlbumPC.class.getClassLoader().getResourceAsStream("images/gumball.rgb565"); //loads in a raw file in rgb565 format, use the windows program paint.net with the rgb565 plug-in to convert png, jpg, etc to this format
		//BitmapInputStream = PixelAlbumPC.class.getClassLoader().getResourceAsStream("images/ginseng.rgb565"); 
	    // loadRGB565();
	    //**************************************************************************
	    
		//************* this set of code loads a PNG file and then converts to RGB565 (the format the LED matrix needs) and then loads on the matrix, it will also handle scaling if the image is not in 32x32 ************
	    WriteImagetoMatrix();
	    loadRGB565PNG();
	    //***************************************************************************
	    
	}	
	
	
	 private static void loadRGB565() {
		   
			try {
	   			int n = BitmapInputStream.read(BitmapBytes, 0, BitmapBytes.length); // reads
	   																				// the
	   																				// input
	   																				// stream
	   																				// into
	   																				// a
	   																				// byte
	   																				// array
	   			Arrays.fill(BitmapBytes, n, BitmapBytes.length, (byte) 0);
	   		} catch (IOException e) {
	   			e.printStackTrace();
	   		}

	   		int y = 0;
	   		for (int i = 0; i < frame_.length; i++) {
	   			frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
	   			y = y + 2;
	   		}
		   
	   }
	 
	 private static void loadRGB565PNG() {
		   
			
	   		int y = 0;
	   		for (int i = 0; i < frame_.length; i++) {
	   			frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
	   			y = y + 2;
	   		}
		   
	   }
	
	
	   private static void WriteImagetoMatrix() throws ConnectionLostException {  //here we'll take a PNG, BMP, or whatever and convert it to RGB565 via a canvas, also we'll re-size the image if necessary
	    	
	  	  //   originalImage = BitmapFactory.decodeFile(imagePath);  
	  	   URL url = PixelAlbumPC.class.getClassLoader().getResource("images/gumball.png");
	  	   
	  	   try {
			originalImage = ImageIO.read(url);
			width_original = originalImage.getWidth();
			height_original = originalImage.getHeight();
			//Log.w(TAG, "width: " + width_original);
			
			 if (width_original != KIND.width || height_original != KIND.height) {  //the image is not the right dimensions, ie, 32px by 32px
		  		 ///***************
				 //Log.w(TAG, "went to resize");
				 ResizedImage = new BufferedImage(KIND.width, KIND.height, originalImage.getType());
				 Graphics2D g = ResizedImage.createGraphics();
				 g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				 g.drawImage(originalImage, 0, 0, KIND.width, KIND.height, 0, 0, originalImage.getWidth(), originalImage.getHeight(), null);
				 g.dispose(); 
				 originalImage = ResizedImage;
		  		 ///******************
			 }
						
			 int numByte=0;
             int i = 0;
             int j = 0;
             int len = BitmapBytes.length;

             for (i = 0; i < KIND.height; i++) {
                 for (j = 0; j < KIND.width; j++) {     

                     Color c = new Color(originalImage.getRGB(j, i));  //i and j were reversed which was rotationg the image by 90 degrees
                     int aRGBpix = originalImage.getRGB(j, i);  //i and j were reversed which was rotationg the image by 90 degrees
                     int alpha;
                     int red = c.getRed();
                     int green = c.getGreen();
                     int blue = c.getBlue();

                     //RGB888
                     //  red = (aRGBpix >> 16) & 0x0FF;
                     //  green = (aRGBpix >> 8) & 0x0FF;
                     //  blue = (aRGBpix >> 0) & 0x0FF; 
                     //  alpha = (aRGBpix >> 24) & 0x0FF;

                     //RGB565
                     red = red >> 3;
                     green = green >> 2;
                     blue = blue >> 3;   
                     //A pixel is represented by a 4-byte (32 bit) integer, like so:
                     //00000000 00000000 00000000 11111111
                     //^ Alpha  ^Red     ^Green   ^Blue
                     //Converting to RGB565

                     short pixel_to_send = 0;
                     int pixel_to_send_int = 0;
                     pixel_to_send_int = (red << 11) | (green << 5) | (blue);
                     pixel_to_send = (short) pixel_to_send_int;

                     //dividing into bytes
                     byte byteH=(byte)((pixel_to_send >> 8) & 0x0FF);
                     byte byteL=(byte)(pixel_to_send & 0x0FF);

                     //Writing it to array - High-byte is the first
                    
                     BitmapBytes[numByte+1]=byteH;
                     BitmapBytes[numByte]=byteL;                    
                     numByte+=2;
                 }
             }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
	   
	   private static byte[] Image2Byte(BufferedImage image)
	   {
	       WritableRaster raster = image.getRaster();
	       DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
	       return buffer.getData();
	   }
	   
	   public static byte[] extractBytes (BufferedImage image) throws IOException {
		
		   // get DataBufferBytes from Raster
		   WritableRaster raster = image.getRaster();
		   DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();

		   return ( data.getData() );
		 }

	 
	 private static void writeTest() {

   		
   		for (int i = 0; i < frame_.length; i++) {
   		
   		//	frame_[i] = (short) (((short) 0x00000000 & 0xFF) | (((short) (short) 0x00000000 & 0xFF) << 8));  //all black
   			frame_[i] = (short) (((short) 0xFFF5FFB0 & 0xFF) | (((short) (short) 0xFFF5FFB0 & 0xFF) << 8));  //pink
   			//frame_[i] = (short) (((short) 0xFFFFFFFF & 0xFF) | (((short) (short) 0xFFFFFFFF & 0xFF) << 8));  //all white
   		}
   }

	protected boolean ledOn_;

	@Override
	protected Window createMainWindow(String args[]) {
		// Use native look and feel.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		JFrame frame = new JFrame("PIXELArt");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		
		JToggleButton button = new JToggleButton("LED");
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.setActionCommand(BUTTON_PRESSED);
		button.addActionListener(this);
		contentPane.add(Box.createVerticalGlue());
		contentPane.add(button);
		contentPane.add(Box.createVerticalGlue());

		// Display the window.
		frame.setSize(300, 100);
		frame.setLocationRelativeTo(null); // center it
		frame.setVisible(true);
		return frame;
	}
	
	 

	@Override
	public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
		return new BaseIOIOLooper() {
			private DigitalOutput led_;

			@Override
			protected void setup() throws ConnectionLostException,
					InterruptedException {
			   led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
			   matrix_ = ioio_.openRgbLedMatrix(KIND);	  	
	  		   matrix_.frame(frame_); 
			}

			@Override
			public void loop() throws ConnectionLostException,
					InterruptedException {
				led_.write(!ledOn_);
				Thread.sleep(10);
			}
		};
	}
	
	

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals(BUTTON_PRESSED)) {
			ledOn_ = ((JToggleButton) event.getSource()).isSelected();
		}
	}
}
