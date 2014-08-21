/*
 * Copyright 2014 Heroic Efforts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.heroicefforts.groovy

import groovy.lang.Closure

import java.awt.AlphaComposite
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.font.FontRenderContext
import java.awt.font.LineBreakMeasurer
import java.awt.font.TextAttribute
import java.awt.font.TextLayout
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.text.AttributedCharacterIterator
import java.text.AttributedString
import java.util.Collection
import java.util.Enumeration
import java.util.List
import java.util.Vector

class GroovyExtensions {

	/**
	 * Provide findResults capability without spurious Groovy deprecation warnings.
	 * 
	 * @param itb iterable to collect from
	 * @param c closure to evaluate/transform each element
	 * @return a (sub)collection of tranformed elements
	 */
	public static Collection collectSome(Iterable itb, Closure c) {
		return itb.findResults(c)
	}

	/**
	 * Retrieve a resource from the context class loader 
	 * @param s
	 * @return the resource URL, null if the resource is not found
	 */
	public static URL resource(CharSequence s) {
		return Thread.currentThread().getContextClassLoader().getResource(s.toString())
	}

	/**
	 * Retrieve a the text from a resource retrieved from the context class loader
	 * @param s the resource name
	 * @return the resource text, null if the resource is not found
	 */
	public static String resourceText(CharSequence s) {
		return resource(s)?.getText()
	}

	/**
	 * Retrieve a the bytes from a resource retrieved from the context class loader
	 * @param s the resource name
	 * @return the resource bytes, null if the resource is not found
	 */
	public static byte[] resourceBytes(CharSequence s) {
		return resource(s)?.getBytes()
	}

	/**
	 * Reset the buffered image with alpha fill.
	 * 
	 * @param bi
	 */
	public static void reset(BufferedImage bi) {	
		Graphics2D g2D = bi.getGraphics()
		g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f))
		Rectangle2D.Double rect = new Rectangle2D.Double(0,0,bi.width,bi.height)
		g2D.fill(rect)
		g2D.dispose()
	} 
	
	/**
	 * Draws text to the supplied graphics with line and tab breaks.
	 * 
	 * @param g2 the destination graphics
	 * @param text the text to draw
	 * @param fontDesc the Font.decode(able) font description
	 * @param x bounding box start position x
	 * @param y bounding box start position y
	 * @param w bounding box width
	 * @param h bounding box height
	 */
	public static void drawText(Graphics2D g2, String text, String fontDesc, int x, int y, int w, int h) {
		Font font = Font.decode(fontDesc);
		def lines = text.split('\r?\n').collect {
			if(it.trim().length() == 0)
				return (AttributedCharacterIterator) null
			else {
				def attStr = new AttributedString(it)
				attStr.addAttribute(TextAttribute.FONT, font)
				return attStr
			}
		}
		
		g2.drawText(lines, x, y, w, h)
	}
	
	/**
	 * Draws the attributed strings to the supplied graphics with line and tab breaks.
	 * 
	 * @param g2 the destination graphics
	 * @param attStrings the attributed text strings to draw
	 * @param x bounding box start position x
	 * @param y bounding box start position y
	 * @param w bounding box width
	 * @param h bounding box height
	 */
	public static void drawText(Graphics2D g2, List<AttributedString> attStrings, int x, int y, int w, int h) {
		FontRenderContext frc = g2.getFontRenderContext()
		def lines = attStrings.collectEntries {
			if(!it)
				return [(it):0]
			else {
				def tabCount = 0
				def iter = it.getIterator()
				for(char c = iter.first(); c != AttributedCharacterIterator.DONE; c = iter.next()) {
					if(c == '\t')
						++tabCount
				}
		   
				return [(it.getIterator()):tabCount]
			}
		}

		float verticalPos = y;
		
		lines.each { AttributedCharacterIterator styledText, int tabCount ->
			if(styledText == null) {
				verticalPos += fontMetrics.getHeight()
				return
			}
			
			float leftMargin = x, rightMargin = x+w;
			FontMetrics fontMetrics = g2.getFontMetrics(styledText.getAttribute(TextAttribute.FONT));
			def tabPixels = fontMetrics.stringWidth('    ')
			float[] tabStops = (1..w / tabPixels).collect { x + tabPixels * it };
		
			// assume styledText is an AttributedCharacterIterator, and the number
			// of tabs in styledText is tabCount
		
			int[] tabLocations = new int[tabCount+1];
		
			int i = 0;
			for (char c = styledText.first(); c != styledText.DONE; c = styledText.next()) {
				if (c == '\t') {
					tabLocations[i++] = styledText.getIndex();
				}
			}
			tabLocations[tabCount] = styledText.getEndIndex() - 1;
		
			// Now tabLocations has an entry for every tab's offset in
			// the text.  For convenience, the last entry is tabLocations
			// is the offset of the last character in the text.
		
			LineBreakMeasurer measurer = new LineBreakMeasurer(styledText, frc);
			int currentTab = 0;
		
			while (measurer.getPosition() < styledText.getEndIndex()) {
		
				// Lay out and draw each line.  All segments on a line
				// must be computed before any drawing can occur, since
				// we must know the largest ascent on the line.
				// TextLayouts are computed and stored in a Vector;
				// their horizontal positions are stored in a parallel
				// Vector.
		
				// lineContainsText is true after first segment is drawn
				boolean lineContainsText = false;
				boolean lineComplete = false;
				float maxAscent = 0, maxDescent = 0;
				float horizontalPos = leftMargin;
				Vector layouts = new Vector(1);
				Vector penPositions = new Vector(1);
		
				while (!lineComplete) {
					float wrappingWidth = rightMargin - horizontalPos;
					TextLayout layout =
							measurer.nextLayout(wrappingWidth,
												tabLocations[currentTab]+1,
												lineContainsText);
		
					// layout can be null if lineContainsText is true
					if (layout != null) {
						layouts.addElement(layout);
						penPositions.addElement(new Float(horizontalPos));
						horizontalPos += layout.getAdvance();
						maxAscent = Math.max(maxAscent, layout.getAscent());
						maxDescent = Math.max(maxDescent,
							layout.getDescent() + layout.getLeading());
					} else {
						lineComplete = true;
					}
		
					lineContainsText = true;
		
					if (measurer.getPosition() == tabLocations[currentTab]+1) {
						currentTab++;
					}
		
					if (measurer.getPosition() == styledText.getEndIndex())
						lineComplete = true;
					else if (horizontalPos >= tabStops[tabStops.length-1])
						lineComplete = true;
		
					if (!lineComplete) {
						// move to next tab stop
						int j;
						for (j=0; horizontalPos >= tabStops[j]; j++) {}
						horizontalPos = tabStops[j];
					}
				}
		
				verticalPos += maxAscent;
		
				Enumeration layoutEnum = layouts.elements();
				Enumeration positionEnum = penPositions.elements();
		
				// now iterate through layouts and draw them
				while (layoutEnum.hasMoreElements()) {
					TextLayout nextLayout = (TextLayout) layoutEnum.nextElement();
					Float nextPosition = (Float) positionEnum.nextElement();
					nextLayout.draw(g2, nextPosition.floatValue(), verticalPos);
				}
		
				verticalPos += maxDescent;
			}
		}
	}
	
}
