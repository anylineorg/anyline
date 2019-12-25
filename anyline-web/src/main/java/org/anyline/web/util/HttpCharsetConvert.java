/* 
 * Copyright 2006-2015 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *          
 */
package org.anyline.web.util; 
 
import java.io.IOException;   
import java.io.InputStreamReader;   
import java.io.OutputStreamWriter;   
import java.io.UnsupportedEncodingException;   
import java.nio.charset.Charset;   
import java.util.ArrayList;   
import java.util.List;   
   

import org.anyline.util.ConfigTable;
import org.springframework.http.HttpInputMessage;   
import org.springframework.http.HttpOutputMessage;   
import org.springframework.http.MediaType;   
import org.springframework.http.converter.AbstractHttpMessageConverter;   
import org.springframework.util.FileCopyUtils;   
   
public class HttpCharsetConvert extends AbstractHttpMessageConverter<String> {   
   
    public static final Charset DEFAULT_CHARSET = Charset.forName(ConfigTable.getString("HTTP_ENCODEING","UTF-8"));   
   
    private final List<Charset> availableCharsets;   
   
    private boolean writeAcceptCharset = true;   
   
    public HttpCharsetConvert() {   
        super(new MediaType("text", "plain", DEFAULT_CHARSET), MediaType.ALL);   
        this.availableCharsets = new ArrayList<Charset>(Charset.availableCharsets().values());   
    }   
   
    /**  
     * Indicates whether the {@code Accept-Charset} should be written to any outgoing request.  
     * <p>Default is {@code true}.  
     * @param writeAcceptCharset writeAcceptCharset
     */   
    public void setWriteAcceptCharset(boolean writeAcceptCharset) {   
        this.writeAcceptCharset = writeAcceptCharset;   
    }   
   
    @Override   
    public boolean supports(Class<?> clazz) {   
        return String.class.equals(clazz);   
    }   
   
    @Override   
    protected String readInternal(Class clazz, HttpInputMessage inputMessage) throws IOException {   
        Charset charset = getContentTypeCharset(inputMessage.getHeaders().getContentType());   
        return FileCopyUtils.copyToString(new InputStreamReader(inputMessage.getBody(), charset));   
    }   
   
    @Override   
    protected Long getContentLength(String s, MediaType contentType) {   
        Charset charset = getContentTypeCharset(contentType);   
        try {   
            return (long) s.getBytes(charset.name()).length;   
        }   
        catch (UnsupportedEncodingException ex) {   
            // should not occur   
            throw new InternalError(ex.getMessage());   
        }   
    }   
   
    @Override   
    protected void writeInternal(String s, HttpOutputMessage outputMessage) throws IOException {   
        if (writeAcceptCharset) {   
            outputMessage.getHeaders().setAcceptCharset(getAcceptedCharsets());   
        }   
        Charset charset = getContentTypeCharset(outputMessage.getHeaders().getContentType());   
        FileCopyUtils.copy(s, new OutputStreamWriter(outputMessage.getBody(), charset));   
    }   
   
    /**  
     * Return the list of supported {@link Charset}.  
     *  
     * <p>By default, returns {@link Charset#availableCharsets()}. Can be overridden in subclasses.  
     *  
     * @return the list of accepted charsets  
     */   
    protected List<Charset> getAcceptedCharsets() {   
        return this.availableCharsets;   
    }   
   
    private Charset getContentTypeCharset(MediaType contentType) {   
        if (contentType != null && contentType.getCharSet() != null) {   
            return contentType.getCharSet();   
        }   
        else {   
            return DEFAULT_CHARSET;   
        }   
    }   
   
}  
