/*****************************************************************************
 *
 * Blue Star, a Java Port of .
 * Last Modified : 3/20/2006
 *
 * License:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 *****************************************************************************/

package org.blue.star.common;

import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;
import org.blue.star.base.utils;

public class BluePatternParser extends PatternParser
{
    private class SystemTimePatternConverter extends PatternConverter
    {

       SystemTimePatternConverter(FormattingInfo formattingInfo)
       {
           super(formattingInfo);
       }

       public String convert(LoggingEvent event)
        {
            return String.valueOf(utils.currentTimeInSeconds());
        }

    }


    public BluePatternParser(String pattern)
    {
        super(pattern);
    }

    public void finalizeConverter(char c)
    {
        if(c == 'N')
        {
            addConverter(new SystemTimePatternConverter(formattingInfo));
            currentLiteral.setLength(0);
        }
        else
        {
            super.finalizeConverter(c);
        }
    }
}
