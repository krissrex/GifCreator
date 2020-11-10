package net.kroo.elliot;

// 
//  GifSequenceWriter.java
//  
//  Created by Elliot Kroo on 2009-04-25.
//
// This work is licensed under the Creative Commons Attribution 3.0 Unported
// License. To view a copy of this license, visit
// http://creativecommons.org/licenses/by/3.0/ or send a letter to Creative
// Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.


//CC 3.0 summarized: indicate if changes are made, credit author.
/*
	You are free to:
	Share — copy and redistribute the material in any medium or format
	Adapt — remix, transform, and build upon the material
	for any purpose, even commercially.
	The licensor cannot revoke these freedoms as long as you follow the license terms.
	
	Under the following terms:
	
	Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made.
	You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
	No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

/*
 * http://elliot.kroo.net/software/java/GifSequenceWriter/
 * 
 * Changes:
 * Corrected spelling mistake in constructor params.
 * Changed disposal method from "none".
 * Added comment on delay-attribute.
 * Reformatting for readability
 * Changed metadata comment
 */

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.awt.image.*;
import java.io.*;
import java.util.Iterator;

public class GifSequenceWriter {
  protected ImageWriter gifWriter;
  protected ImageWriteParam imageWriteParam;
  protected IIOMetadata imageMetaData;
  
  /**
   * Creates a new GifSequenceWriter
   * 
   * @param outputStream the ImageOutputStream to be written to
   * @param imageType one of the imageTypes specified in BufferedImage
   * @param timeBetweenFramesMS the time between frames in miliseconds
   * @param loopContinuously whether the gif should loop repeatedly
   * @throws IIOException if no gif ImageWriters are found
   *
   * @author Elliot Kroo (elliot[at]kroo[dot]net)
   */
  public GifSequenceWriter(
      ImageOutputStream outputStream,
      int imageType,
      int timeBetweenFramesMS,
      boolean loopContinuously) throws IIOException, IOException {
	  
    // my method to create a writer
    gifWriter = getWriter(); 
    imageWriteParam = gifWriter.getDefaultWriteParam();
    ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);

    imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);

    String metaFormatName = imageMetaData.getNativeMetadataFormatName();

    IIOMetadataNode root = (IIOMetadataNode)imageMetaData.getAsTree(metaFormatName);
   
    IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");

    graphicsControlExtensionNode.setAttribute("disposalMethod", "restoreToBackgroundColor");
    graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
    graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
    graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(timeBetweenFramesMS / 10)); //Time between frames in 100th of a second.
    graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

    IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
    commentsNode.setAttribute("CommentExtension", "Generated with https://github.com/krissrex/GifCreator");

    IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
    IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
    child.setAttribute("applicationID", "NETSCAPE");
    child.setAttribute("authenticationCode", "2.0");

    int loop = loopContinuously ? 0 : 1;

    child.setUserObject(new byte[] { 
    			0x1, (byte) (loop & 0xFF), (byte) ((loop >> 8) & 0xFF)
    		}); // byte is signed. x & 0xFF makes the result unsigned. Elliot does not explain why he does not just insert 0x1, 0x0 or 0x0
    			// instead of reading the lower and upper bits of a int that only contains 1 or 0.
    
    appExtensionsNode.appendChild(child);

    imageMetaData.setFromTree(metaFormatName, root);
    
    gifWriter.setOutput(outputStream);
    gifWriter.prepareWriteSequence(null);
  }
  
  public void writeToSequence(RenderedImage img) throws IOException {
    gifWriter.writeToSequence( new IIOImage(img, null, imageMetaData), imageWriteParam);
  }
  
  /**
   * Close this GifSequenceWriter object. This does not close the underlying
   * stream, just finishes off the GIF.
   */
  public void close() throws IOException {
    gifWriter.endWriteSequence();    
  }

  /**
   * Returns the first available GIF ImageWriter using 
   * ImageIO.getImageWritersBySuffix("gif").
   * 
   * @return a GIF ImageWriter object
   * @throws IIOException if no GIF image writers are returned
   */
  private static ImageWriter getWriter() throws IIOException {
    Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
    
    if(!iter.hasNext()) {
      throw new IIOException("No GIF Image Writers Exist");
    } else {
      return iter.next();
    }
  }

  /**
   * Returns an existing child node, or creates and returns a new child node (if 
   * the requested node does not exist).
   * 
   * @param rootNode the <tt>IIOMetadataNode</tt> to search for the child node.
   * @param nodeName the name of the child node.
   * 
   * @return the child node, if found or a new node created with the given name.
   */
  private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
    int nNodes = rootNode.getLength();
    for (int i = 0; i < nNodes; i++) {
      if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName)
          == 0) {
        return((IIOMetadataNode) rootNode.item(i));
      }
    }
    IIOMetadataNode node = new IIOMetadataNode(nodeName);
    rootNode.appendChild(node);
    return(node);
  }
  

/*  
  public static void main(String[] args) throws Exception {
    if (args.length > 1) {
      // grab the output image type from the first image in the sequence
      BufferedImage firstImage = ImageIO.read(new File(args[0]));

      // create a new BufferedOutputStream with the last argument
      ImageOutputStream output = 
        new FileImageOutputStream(new File(args[args.length - 1]));
      
      // create a gif sequence with the type of the first image, 1 second
      // between frames, which loops continuously
      GifSequenceWriter writer = 
        new GifSequenceWriter(output, firstImage.getType(), 1, false);
      
      // write out the first image to our sequence...
      writer.writeToSequence(firstImage);
      for(int i=1; i<args.length-1; i++) {
        BufferedImage nextImage = ImageIO.read(new File(args[i]));
        writer.writeToSequence(nextImage);
      }
      
      writer.close();
      output.close();
    } else {
      System.out.println(
        "Usage: java GifSequenceWriter [list of gif files] [output file]");
    }
  }
  */
}
