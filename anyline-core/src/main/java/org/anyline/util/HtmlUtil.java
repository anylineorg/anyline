/*
 * Copyright 2006-2023 www.anyline.org
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
 */



package org.anyline.util;

public class HtmlUtil {

    public static enum ESCAPE  {
        yen                {public String getDisplay(){return"¥";}  public String getName(){return"&yen;";} 	    public String getCode(){return"&#165;";}  	public String getRemark(){return"&#165;";} },
        ordf               {public String getDisplay(){return"ª";}  public String getName(){return"&ordf;";} 	    public String getCode(){return"&#170;";}  	public String getRemark(){return"&#170;";} },
        macr               {public String getDisplay(){return"¯";}  public String getName(){return"&macr;";} 	    public String getCode(){return"&#175;";}  	public String getRemark(){return"&#175;";} },
        acute              {public String getDisplay(){return"´";}  public String getName(){return"&acute;";} 	    public String getCode(){return"&#180;";}  	public String getRemark(){return"&#180;";} },
        sup1               {public String getDisplay(){return"¹";}  public String getName(){return"&sup1;";} 	    public String getCode(){return"&#185;";}  	public String getRemark(){return"&#185;";} },
        frac34             {public String getDisplay(){return"¾";}  public String getName(){return"&frac34;";}  	public String getCode(){return"&#190;";}  	public String getRemark(){return"&#190;";} },
        Atilde             {public String getDisplay(){return"Ã";}  public String getName(){return"&Atilde;";} 	    public String getCode(){return"&#195;";}  	public String getRemark(){return"&#195;";} },
        Egrave             {public String getDisplay(){return"È";}  public String getName(){return"&Egrave;";} 	    public String getCode(){return"&#200;";}  	public String getRemark(){return"&#200;";} },
        Iacute             {public String getDisplay(){return"Í";}  public String getName(){return"&Iacute;";} 	    public String getCode(){return"&#205;";}  	public String getRemark(){return"&#205;";} },
        Ograve             {public String getDisplay(){return"Ò";}  public String getName(){return"&Ograve;";} 	    public String getCode(){return"&#210;";}  	public String getRemark(){return"&#210;";} },
        Uuml               {public String getDisplay(){return"Ü";}  public String getName(){return"&Uuml;";} 	    public String getCode(){return"&#220;";}  	public String getRemark(){return"&#220;";} },
        aacute             {public String getDisplay(){return"á";}  public String getName(){return"&aacute;";}  	public String getCode(){return"&#225;";}  	public String getRemark(){return"&#225;";} },
        aelig              {public String getDisplay(){return"æ";}  public String getName(){return"&aelig;";} 	    public String getCode(){return"&#230;";}  	public String getRemark(){return"&#230;";} },
        euml               {public String getDisplay(){return"ë";}  public String getName(){return"&euml;";} 	    public String getCode(){return"&#235;";}  	public String getRemark(){return"&#235;";} },
        eth                {public String getDisplay(){return"ð";}  public String getName(){return"&eth;";} 	    public String getCode(){return"&#240;";}  	public String getRemark(){return"&#240;";} },
        otilde             {public String getDisplay(){return"õ";}  public String getName(){return"&otilde;";}  	public String getCode(){return"&#245;";}  	public String getRemark(){return"&#245;";} },
        uacute             {public String getDisplay(){return"ú";}  public String getName(){return"&uacute;";} 	    public String getCode(){return"&#250;";}  	public String getRemark(){return"&#250;";} },
        yuml               {public String getDisplay(){return"ÿ";}  public String getName(){return"&yuml;";} 	    public String getCode(){return"&#255;";}  	public String getRemark(){return"&#255;";} },
        fnof               {public String getDisplay(){return"ƒ";}  public String getName(){return"&fnof;";} 	    public String getCode(){return"&#402;";}  	public String getRemark(){return"&#402;";} },
        Epsilon            {public String getDisplay(){return"Ε";}  public String getName(){return"&Epsilon;";} 	public String getCode(){return"&#917;";}  	public String getRemark(){return"&#917;";} },
        Kappa              {public String getDisplay(){return"Κ";}  public String getName(){return"&Kappa;";} 	    public String getCode(){return"&#922;";}  	public String getRemark(){return"&#922;";} },
        Omicron            {public String getDisplay(){return"Ο";}  public String getName(){return"&Omicron;";} 	public String getCode(){return"&#927;";}  	public String getRemark(){return"&#927;";} },
        Upsilon            {public String getDisplay(){return"Υ";}  public String getName(){return"&Upsilon;";} 	public String getCode(){return"&#933;";}  	public String getRemark(){return"&#933;";} },
        alpha              {public String getDisplay(){return"α";}  public String getName(){return"&alpha;";} 	    public String getCode(){return"&#945;";}  	public String getRemark(){return"&#945;";} },
        zeta               {public String getDisplay(){return"ζ";}  public String getName(){return"&zeta;";} 	    public String getCode(){return"&#950;";}  	public String getRemark(){return"&#950;";} },
        lambda             {public String getDisplay(){return"λ";}  public String getName(){return"&lambda;";} 	    public String getCode(){return"&#955;";}  	public String getRemark(){return"&#955;";} },
        pi                 {public String getDisplay(){return"π";}  public String getName(){return"&pi;";} 	        public String getCode(){return"&#960;";}  	public String getRemark(){return"&#960;";} },
        upsilon            {public String getDisplay(){return"υ";}  public String getName(){return"&upsilon;";} 	public String getCode(){return"&#965;";}  	public String getRemark(){return"&#965;";} },
        thetasym           {public String getDisplay(){return"ϑ";}  public String getName(){return"&thetasym;";} 	public String getCode(){return"&#977;";}  	public String getRemark(){return"&#977;";} },
        prime              {public String getDisplay(){return"′";}  public String getName(){return"&prime;";} 	    public String getCode(){return"&#8242;";}  	public String getRemark(){return"&#8242;";} },
        image              {public String getDisplay(){return"ℑ";}  public String getName(){return"&image;";} 	    public String getCode(){return"&#8465;";}  	public String getRemark(){return"&#8465;";} },
        uarr               {public String getDisplay(){return"↑";}  public String getName(){return"&uarr;";} 	    public String getCode(){return"&#8593;";}  	public String getRemark(){return"&#8593;";} },
        lArr               {public String getDisplay(){return"⇐";}  public String getName(){return"&lArr;";} 	    public String getCode(){return"&#8656;";}  	public String getRemark(){return"&#8656;";} },
        forall             {public String getDisplay(){return"∀";}  public String getName(){return"&forall;";} 	    public String getCode(){return"&#8704;";}  	public String getRemark(){return"&#8704;";} },
        isin               {public String getDisplay(){return"∈";}  public String getName(){return"&isin;";} 	    public String getCode(){return"&#8712;";}  	public String getRemark(){return"&#8712;";} },
        minus              {public String getDisplay(){return"−";}  public String getName(){return"&minus;";} 	    public String getCode(){return"&#8722;";}  	public String getRemark(){return"&#8722;";} },
        ang                {public String getDisplay(){return"∠";}  public String getName(){return"&ang;";} 	    public String getCode(){return"&#8736;";}  	public String getRemark(){return"&#8736;";} },
        Int                {public String getDisplay(){return"∫";}  public String getName(){return"&int;";} 	    public String getCode(){return"&#8747;";}  	public String getRemark(){return"&#8747;";} },
        ne                 {public String getDisplay(){return"≠";}  public String getName(){return"&ne;";} 	        public String getCode(){return"&#8800;";}  	public String getRemark(){return"&#8800;";} },
        sup                {public String getDisplay(){return"⊃";}  public String getName(){return"&sup;";} 	    public String getCode(){return"&#8835;";}  	public String getRemark(){return"&#8835;";} },
        otimes             {public String getDisplay(){return"⊗";}  public String getName(){return"&otimes;";} 	    public String getCode(){return"&#8855;";}  	public String getRemark(){return"&#8855;";} },
        lfloor             {public String getDisplay(){return"⌊";}  public String getName(){return"&lfloor;";} 	    public String getCode(){return"&#8970;";}  	public String getRemark(){return"&#8970;";} },
        spades             {public String getDisplay(){return"♠";}  public String getName(){return"&spades;";} 	    public String getCode(){return"&#9824;";}  	public String getRemark(){return"&#9824;";} },
        oelig              {public String getDisplay(){return"œ";}  public String getName(){return"&oelig;";} 	    public String getCode(){return"&#339;";}  	public String getRemark(){return"&#339;";} },
        tilde              {public String getDisplay(){return"˜";}  public String getName(){return"&tilde;";} 	    public String getCode(){return"&#732;";}  	public String getRemark(){return"&#732;";} },
        zwj                {public String getDisplay(){return"‍";}  public String getName(){return"&zwj;";} 	    public String getCode(){return"&#8205;";}  	public String getRemark(){return"&#8205;";} },
        lsquo              {public String getDisplay(){return"‘";}  public String getName(){return"&lsquo;";} 	    public String getCode(){return"&#8216;";}  	public String getRemark(){return"&#8216;";} },
        bdquo              {public String getDisplay(){return"„";}  public String getName(){return"&bdquo;";} 	    public String getCode(){return"&#8222;";}  	public String getRemark(){return"&#8222;";} },
        rsaquo             {public String getDisplay(){return"›";}  public String getName(){return"&rsaquo;";} 	    public String getCode(){return"&#8250;";}  	public String getRemark(){return"&#8250;";} },
        ensp               {public String getDisplay(){return" ";}  public String getName(){return"&ensp;";} 	public String getCode(){return"&#8194;";}  	public String getRemark(){return"&#8194;";} },
        emsp               {public String getDisplay(){return" ";}  public String getName(){return"&emsp;";} 	public String getCode(){return"&#8195;";}  	public String getRemark(){return"&#8195;";} },
        nbsp               {public String getDisplay(){return" ";}  public String getName(){return"&nbsp;";} 	    public String getCode(){return"&#160;";}  	public String getRemark(){return"&#160;";} },
        lt                 {public String getDisplay(){return"<";}  public String getName(){return"&lt;";} 	        public String getCode(){return"&#60;";}  	public String getRemark(){return"&#60;";} },
        gt                 {public String getDisplay(){return">";}  public String getName(){return"&gt;";} 	        public String getCode(){return"&#62;";}  	public String getRemark(){return"&#62;";} },
        amp                {public String getDisplay(){return"&";}  public String getName(){return"&amp;";} 	    public String getCode(){return"&#38;";}  	public String getRemark(){return"&#38;";} },
        quot               {public String getDisplay(){return"\"";}  public String getName(){return"&quot;";} 	    public String getCode(){return"&#34;";}  	public String getRemark(){return"&#34;";} },
        copy               {public String getDisplay(){return"©";}  public String getName(){return"&copy;";} 	    public String getCode(){return"&#169;";}  	public String getRemark(){return"&#169;";} },
        reg                {public String getDisplay(){return"®";}  public String getName(){return"&reg;";} 	    public String getCode(){return"&#174;";}  	public String getRemark(){return"&#174;";} },
        divide             {public String getDisplay(){return"÷";}  public String getName(){return"&divide;";}  	public String getCode(){return"&#247;";}  	public String getRemark(){return"&#247;";} },
        iexcl              {public String getDisplay(){return"¡";}  public String getName(){return"&iexcl;";} 	    public String getCode(){return"&#161;";}  	public String getRemark(){return"&#161;";} },
        brvbar             {public String getDisplay(){return"¦";}  public String getName(){return"&brvbar;";} 	    public String getCode(){return"&#166;";}  	public String getRemark(){return"&#166;";} },
        laquo              {public String getDisplay(){return"«";}  public String getName(){return"&laquo;";} 	    public String getCode(){return"&#171;";}  	public String getRemark(){return"&#171;";} },
        deg                {public String getDisplay(){return"°";}  public String getName(){return"&deg;";} 	    public String getCode(){return"&#176;";}  	public String getRemark(){return"&#176;";} },
        micro              {public String getDisplay(){return"µ";}  public String getName(){return"&micro;";} 	    public String getCode(){return"&#181;";}  	public String getRemark(){return"&#181;";} },
        ordm               {public String getDisplay(){return"º";}  public String getName(){return"&ordm;";} 	    public String getCode(){return"&#186;";}  	public String getRemark(){return"&#186;";} },
        iquest             {public String getDisplay(){return"¿";}  public String getName(){return"&iquest;";} 	    public String getCode(){return"&#191;";}  	public String getRemark(){return"&#191;";} },
        Auml               {public String getDisplay(){return"Ä";}  public String getName(){return"&Auml;";} 	    public String getCode(){return"&#196;";}  	public String getRemark(){return"&#196;";} },
        Eacute             {public String getDisplay(){return"É";}  public String getName(){return"&Eacute;";}  	public String getCode(){return"&#201;";}  	public String getRemark(){return"&#201;";} },
        Icirc              {public String getDisplay(){return"Î";}  public String getName(){return"&Icirc;";} 	    public String getCode(){return"&#206;";}  	public String getRemark(){return"&#206;";} },
        Oacute             {public String getDisplay(){return"Ó";}  public String getName(){return"&Oacute;";} 	    public String getCode(){return"&#211;";}  	public String getRemark(){return"&#211;";} },
        Oslash             {public String getDisplay(){return"Ø";}  public String getName(){return"&Oslash;";} 	    public String getCode(){return"&#216;";}  	public String getRemark(){return"&#216;";} },
        Yacute             {public String getDisplay(){return"Ý";}  public String getName(){return"&Yacute;";} 	    public String getCode(){return"&#221;";}  	public String getRemark(){return"&#221;";} },
        acirc              {public String getDisplay(){return"â";}  public String getName(){return"&acirc;";} 	    public String getCode(){return"&#226;";}  	public String getRemark(){return"&#226;";} },
        ccedil             {public String getDisplay(){return"ç";}  public String getName(){return"&ccedil;";} 	    public String getCode(){return"&#231;";}  	public String getRemark(){return"&#231;";} },
        igrave             {public String getDisplay(){return"ì";}  public String getName(){return"&igrave;";} 	    public String getCode(){return"&#236;";}  	public String getRemark(){return"&#236;";} },
        ntilde             {public String getDisplay(){return"ñ";}  public String getName(){return"&ntilde;";} 	    public String getCode(){return"&#241;";}  	public String getRemark(){return"&#241;";} },
        ouml               {public String getDisplay(){return"ö";}  public String getName(){return"&ouml;";} 	    public String getCode(){return"&#246;";}  	public String getRemark(){return"&#246;";} },
        ucirc              {public String getDisplay(){return"û";}  public String getName(){return"&ucirc;";} 	    public String getCode(){return"&#251;";}  	public String getRemark(){return"&#251;";} },
        Alpha              {public String getDisplay(){return"Α";}  public String getName(){return"&Alpha;";} 	    public String getCode(){return"&#913;";}  	public String getRemark(){return"&#913;";} },
        Zeta               {public String getDisplay(){return"Ζ";}  public String getName(){return"&Zeta;";} 	    public String getCode(){return"&#918;";}  	public String getRemark(){return"&#918;";} },
        Lambda             {public String getDisplay(){return"Λ";}  public String getName(){return"&Lambda;";} 	    public String getCode(){return"&#923;";}  	public String getRemark(){return"&#923;";} },
        Pi                 {public String getDisplay(){return"Π";}  public String getName(){return"&Pi;";} 	        public String getCode(){return"&#928;";}  	public String getRemark(){return"&#928;";} },
        Phi                {public String getDisplay(){return"Φ";}  public String getName(){return"&Phi;";} 	    public String getCode(){return"&#934;";}  	public String getRemark(){return"&#934;";} },
        beta               {public String getDisplay(){return"β";}  public String getName(){return"&beta;";} 	    public String getCode(){return"&#946;";}  	public String getRemark(){return"&#946;";} },
        eta                {public String getDisplay(){return"η";}  public String getName(){return"&eta;";} 	    public String getCode(){return"&#951;";}  	public String getRemark(){return"&#951;";} },
        mu                 {public String getDisplay(){return"μ";}  public String getName(){return"&mu;";} 	        public String getCode(){return"&#956;";}  	public String getRemark(){return"&#956;";} },
        rho                {public String getDisplay(){return"ρ";}  public String getName(){return"&rho;";} 	    public String getCode(){return"&#961;";}  	public String getRemark(){return"&#961;";} },
        phi                {public String getDisplay(){return"φ";}  public String getName(){return"&phi;";} 	    public String getCode(){return"&#966;";}  	public String getRemark(){return"&#966;";} },
        upsih              {public String getDisplay(){return"ϒ";}  public String getName(){return"&upsih;";} 	    public String getCode(){return"&#978;";}  	public String getRemark(){return"&#978;";} },
        Prime              {public String getDisplay(){return"″";}  public String getName(){return"&Prime;";} 	    public String getCode(){return"&#8243;";}  	public String getRemark(){return"&#8243;";} },
        real               {public String getDisplay(){return"ℜ";}  public String getName(){return"&real;";} 	    public String getCode(){return"&#8476;";}  	public String getRemark(){return"&#8476;";} },
        rarr               {public String getDisplay(){return"→";}  public String getName(){return"&rarr;";} 	    public String getCode(){return"&#8594;";}  	public String getRemark(){return"&#8594;";} },
        uArr               {public String getDisplay(){return"⇑";}  public String getName(){return"&uArr;";} 	    public String getCode(){return"&#8657;";}  	public String getRemark(){return"&#8657;";} },
        part               {public String getDisplay(){return"∂";}  public String getName(){return"&part;";} 	    public String getCode(){return"&#8706;";}  	public String getRemark(){return"&#8706;";} },
        notin              {public String getDisplay(){return"∉";}  public String getName(){return"&notin;";} 	    public String getCode(){return"&#8713;";}  	public String getRemark(){return"&#8713;";} },
        lowast             {public String getDisplay(){return"∗";}  public String getName(){return"&lowast;";} 	    public String getCode(){return"&#8727;";}  	public String getRemark(){return"&#8727;";} },
        and                {public String getDisplay(){return"∧";}  public String getName(){return"&and;";} 	    public String getCode(){return"&#8743;";}  	public String getRemark(){return"&#8743;";} },
        there4             {public String getDisplay(){return"∴";}  public String getName(){return"&there4;";} 	    public String getCode(){return"&#8756;";}  	public String getRemark(){return"&#8756;";} },
        equiv              {public String getDisplay(){return"≡";}  public String getName(){return"&equiv;";} 	    public String getCode(){return"&#8801;";}  	public String getRemark(){return"&#8801;";} },
        nsub               {public String getDisplay(){return"⊄";}  public String getName(){return"&nsub;";} 	    public String getCode(){return"&#8836;";}  	public String getRemark(){return"&#8836;";} },
        perp               {public String getDisplay(){return"⊥";}  public String getName(){return"&perp;";} 	    public String getCode(){return"&#8869;";}  	public String getRemark(){return"&#8869;";} },
        rfloor             {public String getDisplay(){return"⌋";}  public String getName(){return"&rfloor;";} 	    public String getCode(){return"&#8971;";}  	public String getRemark(){return"&#8971;";} },
        clubs              {public String getDisplay(){return"♣";}  public String getName(){return"&clubs;";} 	    public String getCode(){return"&#9827;";}  	public String getRemark(){return"&#9827;";} },
        Scaron             {public String getDisplay(){return"Š";}  public String getName(){return"&Scaron;";} 	    public String getCode(){return"&#352;";}  	public String getRemark(){return"&#352;";} },
        lrm                {public String getDisplay(){return"‎";}  public String getName(){return"&lrm;";} 	    public String getCode(){return"&#8206;";}  	public String getRemark(){return"&#8206;";} },
        rsquo              {public String getDisplay(){return"’";}  public String getName(){return"&rsquo;";} 	    public String getCode(){return"&#8217;";}  	public String getRemark(){return"&#8217;";} },
        dagger             {public String getDisplay(){return"†";}  public String getName(){return"&dagger;";} 	    public String getCode(){return"&#8224;";}  	public String getRemark(){return"&#8224;";} },
        euro               {public String getDisplay(){return"€";}  public String getName(){return"&euro;";} 	    public String getCode(){return"&#8364;";}  	public String getRemark(){return"&#8364;";} },
        cent               {public String getDisplay(){return"¢";}  public String getName(){return"&cent;";} 	    public String getCode(){return"&#162;";}  	public String getRemark(){return"&#162;";} },
        sect               {public String getDisplay(){return"§";}  public String getName(){return"&sect;";} 	    public String getCode(){return"&#167;";}  	public String getRemark(){return"&#167;";} },
        not                {public String getDisplay(){return"¬";}  public String getName(){return"&not;";} 	    public String getCode(){return"&#172;";}  	public String getRemark(){return"&#172;";} },
        plusmn             {public String getDisplay(){return"±";}  public String getName(){return"&plusmn;";} 	    public String getCode(){return"&#177;";}  	public String getRemark(){return"&#177;";} },
        para               {public String getDisplay(){return"¶";}  public String getName(){return"&para;";} 	    public String getCode(){return"&#182;";}  	public String getRemark(){return"&#182;";} },
        raquo              {public String getDisplay(){return"»";}  public String getName(){return"&raquo;";} 	    public String getCode(){return"&#187;";}  	public String getRemark(){return"&#187;";} },
        Agrave             {public String getDisplay(){return"À";}  public String getName(){return"&Agrave;";} 	    public String getCode(){return"&#192;";}  	public String getRemark(){return"&#192;";} },
        Aring              {public String getDisplay(){return"Å";}  public String getName(){return"&Aring;";} 	    public String getCode(){return"&#197;";}  	public String getRemark(){return"&#197;";} },
        Ecirc              {public String getDisplay(){return"Ê";}  public String getName(){return"&Ecirc;";} 	    public String getCode(){return"&#202;";}  	public String getRemark(){return"&#202;";} },
        Iuml               {public String getDisplay(){return"Ï";}  public String getName(){return"&Iuml;";} 	    public String getCode(){return"&#207;";}  	public String getRemark(){return"&#207;";} },
        Ocirc              {public String getDisplay(){return"Ô";}  public String getName(){return"&Ocirc;";} 	    public String getCode(){return"&#212;";}  	public String getRemark(){return"&#212;";} },
        Ugrave             {public String getDisplay(){return"Ù";}  public String getName(){return"&Ugrave;";} 	    public String getCode(){return"&#217;";}  	public String getRemark(){return"&#217;";} },
        THORN              {public String getDisplay(){return"Þ";}  public String getName(){return"&THORN;";} 	    public String getCode(){return"&#222;";}  	public String getRemark(){return"&#222;";} },
        atilde             {public String getDisplay(){return"ã";}  public String getName(){return"&atilde;";} 	    public String getCode(){return"&#227;";}  	public String getRemark(){return"&#227;";} },
        egrave             {public String getDisplay(){return"è";}  public String getName(){return"&egrave;";} 	    public String getCode(){return"&#232;";}  	public String getRemark(){return"&#232;";} },
        iacute             {public String getDisplay(){return"í";}  public String getName(){return"&iacute;";} 	    public String getCode(){return"&#237;";}  	public String getRemark(){return"&#237;";} },
        ograve             {public String getDisplay(){return"ò";}  public String getName(){return"&ograve;";} 	    public String getCode(){return"&#242;";}  	public String getRemark(){return"&#242;";} },
        uuml               {public String getDisplay(){return"ü";}  public String getName(){return"&uuml;";} 	    public String getCode(){return"&#252;";}  	public String getRemark(){return"&#252;";} },
        Beta               {public String getDisplay(){return"Β";}  public String getName(){return"&Beta;";} 	    public String getCode(){return"&#914;";}  	public String getRemark(){return"&#914;";} },
        Eta                {public String getDisplay(){return"Η";}  public String getName(){return"&Eta;";} 	    public String getCode(){return"&#919;";}  	public String getRemark(){return"&#919;";} },
        Mu                 {public String getDisplay(){return"Μ";}  public String getName(){return"&Mu;";} 	        public String getCode(){return"&#924;";}  	public String getRemark(){return"&#924;";} },
        Rho                {public String getDisplay(){return"Ρ";}  public String getName(){return"&Rho;";} 	    public String getCode(){return"&#929;";}  	public String getRemark(){return"&#929;";} },
        Chi                {public String getDisplay(){return"Χ";}  public String getName(){return"&Chi;";} 	    public String getCode(){return"&#935;";}  	public String getRemark(){return"&#935;";} },
        gamma              {public String getDisplay(){return"γ";}  public String getName(){return"&gamma;";} 	    public String getCode(){return"&#947;";}  	public String getRemark(){return"&#947;";} },
        theta              {public String getDisplay(){return"θ";}  public String getName(){return"&theta;";} 	    public String getCode(){return"&#952;";}  	public String getRemark(){return"&#952;";} },
        nu                 {public String getDisplay(){return"ν";}  public String getName(){return"&nu;";} 	        public String getCode(){return"&#957;";}  	public String getRemark(){return"&#957;";} },
        sigmaf             {public String getDisplay(){return"ς";}  public String getName(){return"&sigmaf;";} 	    public String getCode(){return"&#962;";}  	public String getRemark(){return"&#962;";} },
        chi                {public String getDisplay(){return"χ";}  public String getName(){return"&chi;";} 	    public String getCode(){return"&#967;";}  	public String getRemark(){return"&#967;";} },
        piv                {public String getDisplay(){return"ϖ";}  public String getName(){return"&piv;";} 	    public String getCode(){return"&#982;";}  	public String getRemark(){return"&#982;";} },
        oline              {public String getDisplay(){return"‾";}  public String getName(){return"&oline;";} 	    public String getCode(){return"&#8254;";}  	public String getRemark(){return"&#8254;";} },
        trade              {public String getDisplay(){return"™";}  public String getName(){return"&trade;";} 	    public String getCode(){return"&#8482;";}  	public String getRemark(){return"&#8482;";} },
        darr               {public String getDisplay(){return"↓";}  public String getName(){return"&darr;";} 	    public String getCode(){return"&#8595;";}  	public String getRemark(){return"&#8595;";} },
        rArr               {public String getDisplay(){return"⇒";}  public String getName(){return"&rArr;";} 	    public String getCode(){return"&#8658;";}  	public String getRemark(){return"&#8658;";} },
        exist              {public String getDisplay(){return"∃";}  public String getName(){return"&exist;";} 	    public String getCode(){return"&#8707;";}  	public String getRemark(){return"&#8707;";} },
        ni                 {public String getDisplay(){return"∋";}  public String getName(){return"&ni;";} 	        public String getCode(){return"&#8715;";}  	public String getRemark(){return"&#8715;";} },
        radic              {public String getDisplay(){return"√";}  public String getName(){return"&radic;";} 	    public String getCode(){return"&#8730;";}  	public String getRemark(){return"&#8730;";} },
        or                 {public String getDisplay(){return"∨";}  public String getName(){return"&or;";} 	        public String getCode(){return"&#8744;";}  	public String getRemark(){return"&#8744;";} },
        sim                {public String getDisplay(){return"∼";}  public String getName(){return"&sim;";} 	    public String getCode(){return"&#8764;";}  	public String getRemark(){return"&#8764;";} },
        le                 {public String getDisplay(){return"≤";}  public String getName(){return"&le;";} 	        public String getCode(){return"&#8804;";}  	public String getRemark(){return"&#8804;";} },
        sube               {public String getDisplay(){return"⊆";}  public String getName(){return"&sube;";} 	    public String getCode(){return"&#8838;";}  	public String getRemark(){return"&#8838;";} },
        sdot               {public String getDisplay(){return"⋅";}  public String getName(){return"&sdot;";} 	    public String getCode(){return"&#8901;";}  	public String getRemark(){return"&#8901;";} },
        lang               {public String getDisplay(){return"⟨";}  public String getName(){return"&lang;";} 	    public String getCode(){return"&#9001;";}  	public String getRemark(){return"&#9001;";} },
        hearts             {public String getDisplay(){return"♥";}  public String getName(){return"&hearts;";} 	    public String getCode(){return"&#9829;";}  	public String getRemark(){return"&#9829;";} },
        scaron             {public String getDisplay(){return"š";}  public String getName(){return"&scaron;";} 	    public String getCode(){return"&#353;";}  	public String getRemark(){return"&#353;";} },
        rlm                {public String getDisplay(){return"‏";}  public String getName(){return"&rlm;";} 	    public String getCode(){return"&#8207;";}  	public String getRemark(){return"&#8207;";} },
        sbquo              {public String getDisplay(){return"‚";}  public String getName(){return"&sbquo;";} 	    public String getCode(){return"&#8218;";}  	public String getRemark(){return"&#8218;";} },
        Dagger             {public String getDisplay(){return"‡";}  public String getName(){return"&Dagger;";}  	public String getCode(){return"&#8225;";}  	public String getRemark(){return"&#8225;";} },
        pound              {public String getDisplay(){return"£";}  public String getName(){return"&pound;";} 	    public String getCode(){return"&#163;";}  	public String getRemark(){return"&#163;";} },
        uml                {public String getDisplay(){return"¨";}  public String getName(){return"&uml;";} 	    public String getCode(){return"&#168;";}  	public String getRemark(){return"&#168;";} },
        shy                {public String getDisplay(){return"­";}  public String getName(){return"&shy;";} 	    public String getCode(){return"&#173;";}  	public String getRemark(){return"&#173;";} },
        sup2               {public String getDisplay(){return"²";}  public String getName(){return"&sup2;";} 	    public String getCode(){return"&#178;";}  	public String getRemark(){return"&#178;";} },
        middot             {public String getDisplay(){return"·";}  public String getName(){return"&middot;";}  	public String getCode(){return"&#183;";}  	public String getRemark(){return"&#183;";} },
        frac14             {public String getDisplay(){return"¼";}  public String getName(){return"&frac14;";} 	    public String getCode(){return"&#188;";}  	public String getRemark(){return"&#188;";} },
        Aacute             {public String getDisplay(){return"Á";}  public String getName(){return"&Aacute;";} 	    public String getCode(){return"&#193;";}  	public String getRemark(){return"&#193;";} },
        AElig              {public String getDisplay(){return"Æ";}  public String getName(){return"&AElig;";} 	    public String getCode(){return"&#198;";}  	public String getRemark(){return"&#198;";} },
        Euml               {public String getDisplay(){return"Ë";}  public String getName(){return"&Euml;";} 	    public String getCode(){return"&#203;";}  	public String getRemark(){return"&#203;";} },
        ETH                {public String getDisplay(){return"Ð";}  public String getName(){return"&ETH;";} 	    public String getCode(){return"&#208;";}  	public String getRemark(){return"&#208;";} },
        Otilde             {public String getDisplay(){return"Õ";}  public String getName(){return"&Otilde;";}  	public String getCode(){return"&#213;";}  	public String getRemark(){return"&#213;";} },
        Uacute             {public String getDisplay(){return"Ú";}  public String getName(){return"&Uacute;";}      public String getCode(){return"&#218;";}  	public String getRemark(){return"&#218;";} },
        szlig              {public String getDisplay(){return"ß";}  public String getName(){return"&szlig;";} 	    public String getCode(){return"&#223;";}  	public String getRemark(){return"&#223;";} },
        auml               {public String getDisplay(){return"ä";}  public String getName(){return"&auml;";} 	    public String getCode(){return"&#228;";}  	public String getRemark(){return"&#228;";} },
        eacute             {public String getDisplay(){return"é";}  public String getName(){return"&eacute;";} 	    public String getCode(){return"&#233;";}  	public String getRemark(){return"&#233;";} },
        icirc              {public String getDisplay(){return"î";}  public String getName(){return"&icirc;";} 	    public String getCode(){return"&#238;";}  	public String getRemark(){return"&#238;";} },
        oacute             {public String getDisplay(){return"ó";}  public String getName(){return"&oacute;";} 	    public String getCode(){return"&#243;";}  	public String getRemark(){return"&#243;";} },
        oslash             {public String getDisplay(){return"ø";}  public String getName(){return"&oslash;";} 	    public String getCode(){return"&#248;";}  	public String getRemark(){return"&#248;";} },
        yacute             {public String getDisplay(){return"ý";}  public String getName(){return"&yacute;";} 	    public String getCode(){return"&#253;";}  	public String getRemark(){return"&#253;";} },
        Gamma              {public String getDisplay(){return"Γ";}  public String getName(){return"&Gamma;";} 	    public String getCode(){return"&#915;";}  	public String getRemark(){return"&#915;";} },
        Theta              {public String getDisplay(){return"Θ";}  public String getName(){return"&Theta;";} 	    public String getCode(){return"&#920;";}  	public String getRemark(){return"&#920;";} },
        Nu                 {public String getDisplay(){return"Ν";}  public String getName(){return"&Nu;";} 	        public String getCode(){return"&#925;";}  	public String getRemark(){return"&#925;";} },
        Sigma              {public String getDisplay(){return"Σ";}  public String getName(){return"&Sigma;";} 	    public String getCode(){return"&#931;";}  	public String getRemark(){return"&#931;";} },
        Psi                {public String getDisplay(){return"Ψ";}  public String getName(){return"&Psi;";} 	    public String getCode(){return"&#936;";}  	public String getRemark(){return"&#936;";} },
        delta              {public String getDisplay(){return"δ";}  public String getName(){return"&delta;";} 	    public String getCode(){return"&#948;";}  	public String getRemark(){return"&#948;";} },
        iota               {public String getDisplay(){return"ι";}  public String getName(){return"&iota;";} 	    public String getCode(){return"&#953;";}  	public String getRemark(){return"&#953;";} },
        xi                 {public String getDisplay(){return"ξ";}  public String getName(){return"&xi;";} 	        public String getCode(){return"&#958;";}  	public String getRemark(){return"&#958;";} },
        sigma              {public String getDisplay(){return"σ";}  public String getName(){return"&sigma;";} 	    public String getCode(){return"&#963;";}  	public String getRemark(){return"&#963;";} },
        psi                {public String getDisplay(){return"ψ";}  public String getName(){return"&psi;";} 	    public String getCode(){return"&#968;";}  	public String getRemark(){return"&#968;";} },
        bull               {public String getDisplay(){return"•";}  public String getName(){return"&bull;";} 	    public String getCode(){return"&#8226;";}  	public String getRemark(){return"&#8226;";} },
        frasl              {public String getDisplay(){return"⁄";}  public String getName(){return"&frasl;";} 	    public String getCode(){return"&#8260;";}  	public String getRemark(){return"&#8260;";} },
        alefsym            {public String getDisplay(){return"ℵ";}  public String getName(){return"&alefsym;";} 	public String getCode(){return"&#8501;";}  	public String getRemark(){return"&#8501;";} },
        harr               {public String getDisplay(){return"↔";}  public String getName(){return"&harr;";} 	    public String getCode(){return"&#8596;";}  	public String getRemark(){return"&#8596;";} },
        dArr               {public String getDisplay(){return"⇓";}  public String getName(){return"&dArr;";} 	    public String getCode(){return"&#8659;";}  	public String getRemark(){return"&#8659;";} },
        empty              {public String getDisplay(){return"∅";}  public String getName(){return"&empty;";} 	    public String getCode(){return"&#8709;";}  	public String getRemark(){return"&#8709;";} },
        prod               {public String getDisplay(){return"∏";}  public String getName(){return"&prod;";} 	    public String getCode(){return"&#8719;";}  	public String getRemark(){return"&#8719;";} },
        prop               {public String getDisplay(){return"∝";}  public String getName(){return"&prop;";} 	    public String getCode(){return"&#8733;";}  	public String getRemark(){return"&#8733;";} },
        cap                {public String getDisplay(){return"∩";}  public String getName(){return"&cap;";} 	    public String getCode(){return"&#8745;";}  	public String getRemark(){return"&#8745;";} },
        cong               {public String getDisplay(){return"≅";}  public String getName(){return"&cong;";} 	    public String getCode(){return"&#8773;";}  	public String getRemark(){return"&#8773;";} },
        ge                 {public String getDisplay(){return"≥";}  public String getName(){return"&ge;";} 	        public String getCode(){return"&#8805;";}  	public String getRemark(){return"&#8805;";} },
        supe               {public String getDisplay(){return"⊇";}  public String getName(){return"&supe;";} 	    public String getCode(){return"&#8839;";}  	public String getRemark(){return"&#8839;";} },
        lceil              {public String getDisplay(){return"⌈";}  public String getName(){return"&lceil;";} 	    public String getCode(){return"&#8968;";}  	public String getRemark(){return"&#8968;";} },
        rang               {public String getDisplay(){return"⟩";}  public String getName(){return"&rang;";} 	    public String getCode(){return"&#9002;";}  	public String getRemark(){return"&#9002;";} },
        diams              {public String getDisplay(){return"♦";}  public String getName(){return"&diams;";} 	    public String getCode(){return"&#9830;";}  	public String getRemark(){return"&#9830;";} },
        Yuml               {public String getDisplay(){return"Ÿ";}  public String getName(){return"&Yuml;";} 	    public String getCode(){return"&#376;";}  	public String getRemark(){return"&#376;";} },
        thinsp             {public String getDisplay(){return"";}  public String getName(){return"&thinsp;";} 	    public String getCode(){return"&#8201;";}  	public String getRemark(){return"&#8201;";} },
        ndash              {public String getDisplay(){return"–";}  public String getName(){return"&ndash;";} 	    public String getCode(){return"&#8211;";}  	public String getRemark(){return"&#8211;";} },
        ldquo              {public String getDisplay(){return"“";}  public String getName(){return"&ldquo;";} 	    public String getCode(){return"&#8220;";}  	public String getRemark(){return"&#8220;";} },
        permil             {public String getDisplay(){return"‰";}  public String getName(){return"&permil;";} 	    public String getCode(){return"&#8240;";}  	public String getRemark(){return"&#8240;";} },
        curren             {public String getDisplay(){return"¤";}  public String getName(){return"&curren;";} 	    public String getCode(){return"&#164;";}  	public String getRemark(){return"&#164;";} },
        sup3               {public String getDisplay(){return"³";}  public String getName(){return"&sup3;";} 	    public String getCode(){return"&#179;";}  	public String getRemark(){return"&#179;";} },
        cedil              {public String getDisplay(){return"¸";}  public String getName(){return"&cedil;";} 	    public String getCode(){return"&#184;";}  	public String getRemark(){return"&#184;";} },
        frac12             {public String getDisplay(){return"½";}  public String getName(){return"&frac12;";} 	    public String getCode(){return"&#189;";}  	public String getRemark(){return"&#189;";} },
        Acirc              {public String getDisplay(){return"Â";}  public String getName(){return"&Acirc;";} 	    public String getCode(){return"&#194;";}  	public String getRemark(){return"&#194;";} },
        Ccedil             {public String getDisplay(){return"Ç";}  public String getName(){return"&Ccedil;";} 	    public String getCode(){return"&#199;";}  	public String getRemark(){return"&#199;";} },
        Igrave             {public String getDisplay(){return"Ì";}  public String getName(){return"&Igrave;";} 	    public String getCode(){return"&#204;";}  	public String getRemark(){return"&#204;";} },
        Ntilde             {public String getDisplay(){return"Ñ";}  public String getName(){return"&Ntilde;";} 	    public String getCode(){return"&#209;";}  	public String getRemark(){return"&#209;";} },
        Ouml               {public String getDisplay(){return"Ö";}  public String getName(){return"&Ouml;";} 	    public String getCode(){return"&#214;";}  	public String getRemark(){return"&#214;";} },
        Ucirc              {public String getDisplay(){return"Û";}  public String getName(){return"&Ucirc;";} 	    public String getCode(){return"&#219;";}  	public String getRemark(){return"&#219;";} },
        agrave             {public String getDisplay(){return"à";}  public String getName(){return"&agrave;";}  	public String getCode(){return"&#224;";}  	public String getRemark(){return"&#224;";} },
        aring              {public String getDisplay(){return"å";}  public String getName(){return"&aring;";} 	    public String getCode(){return"&#229;";}  	public String getRemark(){return"&#229;";} },
        ecirc              {public String getDisplay(){return"ê";}  public String getName(){return"&ecirc;";} 	    public String getCode(){return"&#234;";}  	public String getRemark(){return"&#234;";} },
        iuml               {public String getDisplay(){return"ï";}  public String getName(){return"&iuml;";} 	    public String getCode(){return"&#239;";}  	public String getRemark(){return"&#239;";} },
        ocirc              {public String getDisplay(){return"ô";}  public String getName(){return"&ocirc;";} 	    public String getCode(){return"&#244;";}  	public String getRemark(){return"&#244;";} },
        ugrave             {public String getDisplay(){return"ù";}  public String getName(){return"&ugrave;";} 	    public String getCode(){return"&#249;";}  	public String getRemark(){return"&#249;";} },
        thorn              {public String getDisplay(){return"þ";}  public String getName(){return"&thorn;";} 	    public String getCode(){return"&#254;";}  	public String getRemark(){return"&#254;";} },
        Delta              {public String getDisplay(){return"Δ";}  public String getName(){return"&Delta;";} 	    public String getCode(){return"&#916;";}  	public String getRemark(){return"&#916;";} },
        Iota               {public String getDisplay(){return"Ι";}  public String getName(){return"&Iota;";} 	    public String getCode(){return"&#921;";}  	public String getRemark(){return"&#921;";} },
        Xi                 {public String getDisplay(){return"Ξ";}  public String getName(){return"&Xi;";} 	        public String getCode(){return"&#926;";}  	public String getRemark(){return"&#926;";} },
        Tau                {public String getDisplay(){return"Τ";}  public String getName(){return"&Tau;";} 	    public String getCode(){return"&#932;";}  	public String getRemark(){return"&#932;";} },
        Omega              {public String getDisplay(){return"Ω";}  public String getName(){return"&Omega;";} 	    public String getCode(){return"&#937;";}  	public String getRemark(){return"&#937;";} },
        epsilon            {public String getDisplay(){return"ε";}  public String getName(){return"&epsilon;";} 	public String getCode(){return"&#949;";}  	public String getRemark(){return"&#949;";} },
        kappa              {public String getDisplay(){return"κ";}  public String getName(){return"&kappa;";} 	    public String getCode(){return"&#954;";}  	public String getRemark(){return"&#954;";} },
        omicron            {public String getDisplay(){return"ο";}  public String getName(){return"&omicron;";} 	public String getCode(){return"&#959;";}  	public String getRemark(){return"&#959;";} },
        tau                {public String getDisplay(){return"τ";}  public String getName(){return"&tau;";} 	    public String getCode(){return"&#964;";}  	public String getRemark(){return"&#964;";} },
        omega              {public String getDisplay(){return"ω";}  public String getName(){return"&omega;";} 	    public String getCode(){return"&#969;";}  	public String getRemark(){return"&#969;";} },
        hellip             {public String getDisplay(){return"…";}  public String getName(){return"&hellip;";} 	    public String getCode(){return"&#8230;";}  	public String getRemark(){return"&#8230;";} },
        weierp             {public String getDisplay(){return"℘";}  public String getName(){return"&weierp;";} 	    public String getCode(){return"&#8472;";}  	public String getRemark(){return"&#8472;";} },
        larr               {public String getDisplay(){return"←";}  public String getName(){return"&larr;";} 	    public String getCode(){return"&#8592;";}  	public String getRemark(){return"&#8592;";} },
        crarr              {public String getDisplay(){return"↵";}  public String getName(){return"&crarr;";} 	    public String getCode(){return"&#8629;";}  	public String getRemark(){return"&#8629;";} },
        hArr               {public String getDisplay(){return"⇔";}  public String getName(){return"&hArr;";} 	    public String getCode(){return"&#8660;";}  	public String getRemark(){return"&#8660;";} },
        nabla              {public String getDisplay(){return"∇";}  public String getName(){return"&nabla;";} 	    public String getCode(){return"&#8711;";}  	public String getRemark(){return"&#8711;";} },
        sum                {public String getDisplay(){return"∑";}  public String getName(){return"&sum;";} 	    public String getCode(){return"&#8721;";}  	public String getRemark(){return"&#8721;";} },
        infin              {public String getDisplay(){return"∞";}  public String getName(){return"&infin;";} 	    public String getCode(){return"&#8734;";}  	public String getRemark(){return"&#8734;";} },
        cup                {public String getDisplay(){return"∪";}  public String getName(){return"&cup;";} 	    public String getCode(){return"&#8746;";}  	public String getRemark(){return"&#8746;";} },
        asymp              {public String getDisplay(){return"≈";}  public String getName(){return"&asymp;";} 	    public String getCode(){return"&#8776;";}  	public String getRemark(){return"&#8776;";} },
        sub                {public String getDisplay(){return"⊂";}  public String getName(){return"&sub;";} 	    public String getCode(){return"&#8834;";}  	public String getRemark(){return"&#8834;";} },
        oplus              {public String getDisplay(){return"⊕";}  public String getName(){return"&oplus;";} 	    public String getCode(){return"&#8853;";}  	public String getRemark(){return"&#8853;";} },
        rceil              {public String getDisplay(){return"⌉";}  public String getName(){return"&rceil;";} 	    public String getCode(){return"&#8969;";}  	public String getRemark(){return"&#8969;";} },
        loz                {public String getDisplay(){return"◊";}  public String getName(){return"&loz;";} 	    public String getCode(){return"&#9674;";}  	public String getRemark(){return"&#9674;";} },
        OElig              {public String getDisplay(){return"Œ";}  public String getName(){return"&OElig;";} 	    public String getCode(){return"&#338;";}  	public String getRemark(){return"&#338;";} },
        circ               {public String getDisplay(){return"ˆ";}  public String getName(){return"&circ;";} 	    public String getCode(){return"&#710;";}  	public String getRemark(){return"&#710;";} },
        zwnj               {public String getDisplay(){return"‌";}  public String getName(){return"&zwnj;";} 	public String getCode(){return"&#8204;";}  	public String getRemark(){return"&#8204;";} },
        mdash              {public String getDisplay(){return"—";}  public String getName(){return"&mdash;";} 	    public String getCode(){return"&#8212;";}  	public String getRemark(){return"&#8212;";} },
        rdquo              {public String getDisplay(){return"”";}  public String getName(){return"&rdquo;";} 	    public String getCode(){return"&#8221;";}  	public String getRemark(){return"&#8221;";} },
        lsaquo             {public String getDisplay(){return"‹";}  public String getName(){return"&lsaquo;";} 	    public String getCode(){return"&#8249;";}  	public String getRemark(){return"&#8249;";}};
        public abstract String getCode();
        public abstract String getDisplay();
        public abstract String getName();
        public abstract String getRemark();
    }
    public static String display(String src){
        String result = src;
        for (ESCAPE item : ESCAPE.values()) {
            result = result.replace(item.getName(), item.getDisplay()).replace(item.getCode(), item.getDisplay());
        }
        return result;
    }
    public static String name2code(String src){
        String result = src;
        for (ESCAPE item : ESCAPE.values()) {
            result = result.replace(item.getName(), item.getCode());
        }
        return result;
    }
    public static String code2display(String src){
        String result = src;
        for (ESCAPE item : ESCAPE.values()) {
            result = result.replace(item.getCode(), item.getDisplay());
        }
        return result;
    }

    /**
     * 根据单元格内容生成表格<br/>
     * 什么情况下需要, 通过OCR识别内容时, 如果原文是表格形式, 识别出来的结果排列会乱<br/>
     * <pre>
     * 如：原文是这样
     * ---------------
     * | A | B | C |
     * ---------------
     * | 1 | 2 | 3 |
     * ---------------
     * 识别结果可能会是这样
     * A
     * B
     * C
     * 1
     * 2
     * 3
     * 这时需要HtmlUtil.table(String 识别结果, int 每行几列, String 分隔符)
     * 把识别出来的内容还原成表格形式
     * <pre/>
     * @param cells 单元格内容
     * @param split 分隔符号
     * @param cols 每行cols列
     * @return table
     */
    public static String table(String cells, int cols, String split){
        StringBuilder table = new StringBuilder();
        String[] tds = cells.split(split);
        table.append("<table>");
        for(int i=0; i<tds.length; i++){
            if(i%cols == 0){
                table.append("<tr>");
            }
            table.append("<td>").append(tds[i]).append("</td>");
            if((i+1)%cols == 0){
                table.append("</tr>");
            }
        }
        table.append("</table>");
        return table.toString();
    }

    /**
     * <pre>
     * 单元格内容生成表格
     * 默认换行分隔 \n
     * </pre>
     * @param cells 单元格内容
     * @param cols 每行cols列
     * @return table
     */
    public static String table(String cells, int cols){
        return table(cells, cols, "\n");
    }

    /**
     * 截取html, 截取to位置所在的第一层标签的结束位置, 避免拆破标签, 不支持多层嵌套结构
     * @param html html
     * @param fr fr
     * @param to to
     * @return String
     */
    public static String cut(String html, int fr, int to){
        String chk = BasicUtil.cut(html, fr, to);
        String result = null;
        if(to >= html.length()){
            return html;
        }
        int result_end = chk.length();
        //flag <, >, </, >, /, />
        //     0 1 2  3 4 5
        int chk_last_t0 = chk.lastIndexOf("<");
        if(chk_last_t0 > 0){
            int chk_last_t1 = chk.lastIndexOf("/>");
            int chk_last_t1_ = chk.lastIndexOf("</");
            String after_src_last_t0 = html.substring(chk_last_t0+1, chk_last_t0+2);
            if((chk_last_t1 < chk_last_t0 && chk_last_t1_ < chk_last_t0) || "/".equals(after_src_last_t0)){
                // < 后没有结束
                int src_last_t1 = html.indexOf(">", chk_last_t0);
                if(src_last_t1 > chk_last_t0){
                    // >之前的位置
                    String before_src_last_t1 = html.substring(src_last_t1-1, src_last_t1);
                    if(before_src_last_t1.equals("/")){
                        //<br/>
                        //单标签找到结束位置
                        chk = html.substring(0, src_last_t1+1);
                    }else{
                        //<之后的位置
                        if(after_src_last_t0.equals("/")){
                            // </a>
                            //如果是end标签, 找到end标签结束位置
                            int end_tag_tag = html.indexOf(">", chk_last_t0);
                            chk = html.substring(0, end_tag_tag+1);
                        }else {
                            // <a href=''
                            //如果是start标签, 找到相应的end标签
                            int start_tag_end = html.indexOf(">", chk_last_t0+1 );
                            int end_tag_end = html.indexOf(">", start_tag_end+1);
                            chk = html.substring(0, end_tag_end+1);
                        }
                    }
                    result = chk;
                }
            }
        }
        if(null == result){
            result = chk;
        }
        return result;
    }
}
