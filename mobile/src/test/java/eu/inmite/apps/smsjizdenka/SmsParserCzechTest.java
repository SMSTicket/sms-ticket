/*
 * Copyright 2015 AVAST Software s.r.o.
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

package eu.inmite.apps.smsjizdenka;

import java.util.Date;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.inmite.apps.smsjizdenka.util.SmsParser;
import eu.inmite.apps.smsjizdenka.util.Ticket;

/**
 * Tests of definitions for the Czech SMS Tickets.
 * <p/>
 * If you want to add more tickets/cities, write unit test first. The it's guaranteed it will run in the app.
 *
 * @author David Vávra (vavra@avast.com)
 */
public class SmsParserCzechTest {

    static SmsParser sSmsParser;

    @BeforeClass
    public static void setup() {
        sSmsParser = new SmsParser("ces");
        // "ces" = app in Czech language
        // "" = app in English language
    }


    // ------ Praha
    @Test
    public void testDpt24() {
        Ticket ticket = sSmsParser.parse("90206024", "DP hl.m.Prahy, a.s., Jizdenka prestupni 24,- Kc, Platnost od: 11.7.11 19:29  do: 11.7.11 19:59. Pouze v pasmu P. bhAJpWP9B / 861418");
        Assert.assertEquals("Praha", ticket.city);
        Assert.assertEquals(24, ticket.price, 0);
        Assert.assertEquals("bhAJpWP9B / 861418", ticket.hash);
        Assert.assertEquals(new Date("July 11, 2011, 19:29:00"), ticket.validFrom);
        Assert.assertEquals(new Date("July 11, 2011, 19:59:00"), ticket.validTo);
    }

    @Test
    public void testDpt32() {
        Ticket ticket = sSmsParser.parse("90206032", "DP hl.m.Prahy, a.s., Jizdenka prestupni 32,- Kc, Platnost od: 29.8.11 8:09  do: 29.8.11 9:39. Pouze v pasmu P. WzL9n3JuQ / 169605");
        Assert.assertEquals("Praha", ticket.city);
        Assert.assertEquals(32, ticket.price, 0);
        Assert.assertEquals("WzL9n3JuQ / 169605", ticket.hash);
        Assert.assertEquals(new Date("August 29, 2011, 8:09:00"), ticket.validFrom);
        Assert.assertEquals(new Date("August 29, 2011, 9:39:00"), ticket.validTo);
    }

    @Test
    public void testDpt110() {
        Ticket ticket = sSmsParser.parse("90206110", "DP hl.m.Prahy, a.s., Jizdenka prestupni 110,- Kc, Platnost od: 3.7.11 19:50  do: 4.7.11 19:50. Pouze v pasmu P. yjCNXTCEc / 5892179");
        Assert.assertEquals("Praha", ticket.city);
        Assert.assertEquals(110, ticket.price, 0);
        Assert.assertEquals("yjCNXTCEc / 5892179", ticket.hash);
        Assert.assertEquals(new Date("July 3, 2011, 19:50:00"), ticket.validFrom);
        Assert.assertEquals(new Date("July 4, 2011, 19:50:00"), ticket.validTo);
    }

    @Test
    public void testDpt310() {
        new UnsupportedOperationException("Not implemented yet");
    }

    // ------ Usti nad Labem
    @Test
    public void testDpj() {
        Ticket ticket = sSmsParser.parse("90206020", "849tYQEec/014268/Of9 DP m. Usti n/L a.s. Jizdenka prestupni 20,- Kc, Platnost od: 11.6.13 18:55 do: 11.6.13 19:55 Plati v zonach 101, 111, 121, 122.");
        Assert.assertEquals("Ústí nad Labem", ticket.city);
        Assert.assertEquals(20, ticket.price, 0);
        Assert.assertEquals("849tYQEec/014268/Of9", ticket.hash);
        Assert.assertEquals(new Date("Jun 11, 2013, 18:55:00"), ticket.validFrom);
        Assert.assertEquals(new Date("Jun 11, 2013, 19:55:00"), ticket.validTo);
    }

    // ------ Ostrava
    @Test
    public void testOstravaDpo70() {
        // verified 22.3.2016
        Ticket ticket = sSmsParser.parse("90230030", "Q1-01-52 70-104 DP Ostrava Jizdenka prestupni 30 Kc.Plati od 9.3.2016 14:57h do 9.3.2016 16:07h jen ve spojich DPO.Chcete priste platit kartou? mccz.eu/VV6R");
        Assert.assertEquals("Ostrava", ticket.city);
        Assert.assertEquals(30, ticket.price, 0);
        Assert.assertEquals("Q1-01-52 70-104", ticket.hash);
        Assert.assertEquals(new Date("March 9, 2016, 14:57:00"), ticket.validFrom);
        Assert.assertEquals(new Date("March 9, 2016, 16:07:00"), ticket.validTo);
    }

    @Test
    public void testOstravaDpo70z() {
        // verified 22.3.2016
        Ticket ticket = sSmsParser.parse("90230015", "Q1-01-52 70-104 DP Ostrava Jizdenka prestupni 15 Kc.Plati od 9.3.2016 14:57h do 9.3.2016 16:07h jen ve spojich DPO.Chcete priste platit kartou? mccz.eu/VV6R");
        Assert.assertEquals("Ostrava", ticket.city);
        Assert.assertEquals(15, ticket.price, 0);
        Assert.assertEquals("Q1-01-52 70-104", ticket.hash);
        Assert.assertEquals(new Date("March 9, 2016, 14:57:00"), ticket.validFrom);
        Assert.assertEquals(new Date("March 9, 2016, 16:07:00"), ticket.validTo);
    }

    // ------ Ceske Budejovice
    @Test
    public void testBud() {
        Ticket ticket = sSmsParser.parse("90206025", "DPMCB, a.s., SMS jizdenka plnocenna prestupni 25,- Kc, Platnost od: 30.8.11 15:17 do: 30.8.11 15:47. SdyezdMV9 / 998638");
        Assert.assertEquals("České Budějovice", ticket.city);
        Assert.assertEquals(25, ticket.price, 0);
        Assert.assertEquals("SdyezdMV9 / 998638", ticket.hash);
        Assert.assertEquals(new Date("August 30, 2011, 15:17:00"), ticket.validFrom);
        Assert.assertEquals(new Date("August 30, 2011, 15:47:00"), ticket.validTo);
    }

    @Test
    public void testBud24() {
        Ticket ticket = sSmsParser.parse("90206070", "DPMCB, a.s., SMS jizdenka plnocenna prestupni 70,- Kc, Platnost od: 2.11.09 13:47; do: 3.11.09 13:47. wPzAEAwc8 / 925562");
        Assert.assertEquals("České Budějovice", ticket.city);
        Assert.assertEquals(70, ticket.price, 0);
        Assert.assertEquals("wPzAEAwc8 / 925562", ticket.hash);
        Assert.assertEquals(new Date("November 2, 2009, 13:47:00"), ticket.validFrom);
        Assert.assertEquals(new Date("November 3, 2009, 13:47:00"), ticket.validTo);
    }


    // ------ Liberec
    @Test
    public void testLib() {
       /* Ticket ticket = sSmsParser.parse("90206025", "Zona Liberec, linky MHD DPMLJ, Jizdenka zakladni prestupni 25,- Kc, Plati od: 2.1.12 10:54 do: 2.1.12 11:54. KMyzC9U7s / 160852");
        Assert.assertEquals("Liberec", ticket.city);
        Assert.assertEquals(25, ticket.price, 0);
        Assert.assertEquals("KMyzC9U7s / 160852", ticket.hash);
        Assert.assertEquals(new Date("January 1, 2012, 10:54:00"), ticket.validFrom);
        Assert.assertEquals(new Date("January 1, 2012, 11:54:00"), ticket.validTo);*/
        new UnsupportedOperationException("Needs new SMS definition");
    }

    // ------ Liberec - Jablonec nad Nisou
    @Test
    public void testLib31() {
        Ticket ticket = sSmsParser.parse("90206031", "Zona LBC+JBC, linka 11 MHD DPMLJ, Jizdenka zakl. neprestupni 31,- Kc, Plati od: 25.6.15 18:53 do: 25.6.15 19:53. CoSjdY5CQ / 350149");
        Assert.assertEquals("Liberec", ticket.city);
        Assert.assertEquals(31, ticket.price, 0);
        Assert.assertEquals("CoSjdY5CQ / 350149", ticket.hash);
        Assert.assertEquals(new Date("June 25, 2015, 18:53:00"), ticket.validFrom);
        Assert.assertEquals(new Date("June 25, 2015, 19:53:00"), ticket.validTo);
    }

    // ------ Plzen
    @Test
    public void testPmd35m() {
        Ticket ticket = sSmsParser.parse("90206020", "PMDP, a.s. Jizdenka prestupni 20 Kc, Platnost od: 9.1.12 18:42 do: 9.1.12 19:17. Plati ve vozidlech PMDP, a.s. je3Bq9dDd / 644949");
        Assert.assertEquals("Plzeň", ticket.city);
        Assert.assertEquals(20, ticket.price, 0);
        Assert.assertEquals("je3Bq9dDd / 644949", ticket.hash);
        Assert.assertEquals(new Date("January 9, 2012, 18:42:00"), ticket.validFrom);
        Assert.assertEquals(new Date("January 9, 2012, 19:17:00"), ticket.validTo);
    }

    // ------ Olomouc
    @Test
    public void testDpmo() {
        Ticket ticket = sSmsParser.parse("90206018", "DP m. Olomouce, a.s. SMS jizdenka prestupni 18 Kc Platnost: Od 3.9.12 16:56 Do: 3.9.12 17:46 Pouze linky DPMO 1 az 111 ncqur4RBr/223541");
        Assert.assertEquals("Olomouc", ticket.city);
        Assert.assertEquals(18, ticket.price, 0);
        Assert.assertEquals("ncqur4RBr/223541", ticket.hash);
        Assert.assertEquals(new Date("September 3, 2012, 16:56:00"), ticket.validFrom);
        Assert.assertEquals(new Date("September 3, 2012, 17:46:00"), ticket.validTo);
    }

    // ------ Pardubice
    @Test
    public void testDpmp() {
        /*Ticket ticket = sSmsParser.parse("90206025", "DPMP a.s. SMS jizdenka prestupni pro zonu I+II 25 Kc. Platnost od: 5.2.13 9:16 do: 5.2.13 10:01 qii3LqjjE/228335/fWR");
        Assert.assertEquals("Pardubice", ticket.city);
        Assert.assertEquals(25, ticket.price, 0);
        Assert.assertEquals("qii3LqjjE/228335/fWR", ticket.hash);
        Assert.assertEquals(new Date("February 5, 2012, 9:16:00"), ticket.validFrom);
        Assert.assertEquals(new Date("February 5, 2012, 10:01:00"), ticket.validTo);*/
        new UnsupportedOperationException("Needs new SMS definition");
    }

    @Test
    public void testDpmp24() {
        /*Ticket ticket = sSmsParser.parse("90206065", "DPMP a.s. SMS jizdenka prestupni pro zonu I+II 65 Kc. Platnost od: 5.2.13 9:15 do: 6.2.13 9:15 ArxFyuqHP/669942/MWf");
        Assert.assertEquals("Pardubice", ticket.city);
        Assert.assertEquals(65, ticket.price, 0);
        Assert.assertEquals("ArxFyuqHP/669942/MWf", ticket.hash);
        Assert.assertEquals(new Date("February 5, 2012, 9:15:00"), ticket.validFrom);
        Assert.assertEquals(new Date("February 6, 2012, 9:15:00"), ticket.validTo);*/
        new UnsupportedOperationException("Needs new SMS definition");
    }

    // ------ Hradec Kralove
    @Test
    public void testHk() {
        Ticket ticket = sSmsParser.parse("90230025", "Kod: 86-40-69 66-621 DPMHK a.s. - SMS jizdenka prestupni. Cena 25 Kc vc. DPH. Platna od 4.10.2016 11:48 do 4.10.2016 12:33.");
        Assert.assertEquals("Hradec Králové", ticket.city);
        Assert.assertEquals(25, ticket.price, 0);
        Assert.assertEquals("86-40-69 66-621", ticket.hash);
        Assert.assertEquals(new Date("October 4, 2016, 11:48:00"), ticket.validFrom);
        Assert.assertEquals(new Date("October 4, 2016, 12:33:00"), ticket.validTo);
    }

    @Test
    public void testHk24() {
        Ticket ticket = sSmsParser.parse("90230080", "Kod: 32-52-78 25-519 DPMHK a.s. - SMS jizdenka prestupni. Cena 80 Kc vc. DPH. Platna od 16.1.2012 10:12 do 17.1.2012 10:12.");
        Assert.assertEquals("Hradec Králové", ticket.city);
        Assert.assertEquals(80, ticket.price, 0);
        Assert.assertEquals("32-52-78 25-519", ticket.hash);
        Assert.assertEquals(new Date("January 16, 2012, 10:12:00"), ticket.validFrom);
        Assert.assertEquals(new Date("January 17, 2012, 10:12:00"), ticket.validTo);
    }

    // ------ Karlovy Vary
    @Test
    public void testJvk25() {
        Ticket ticket = sSmsParser.parse("90230025", "68-702 65-56-36 DPKV a.s.-SMS jizdenka prestupni.Cena 25 Kc vc.DPH.Platna od 4.10.2016 12:54 do 4.10.2016 13:54.Platba kartou https://kj.maternacz.com/aaaa");
        Assert.assertEquals("Karlovy Vary", ticket.city);
        Assert.assertEquals(25, ticket.price, 0);
        Assert.assertEquals("68-702 65-56-36", ticket.hash);
        Assert.assertEquals(new Date("October 4, 2016, 12:54:00"), ticket.validFrom);
        Assert.assertEquals(new Date("October 4, 2016, 13:54:00"), ticket.validTo);
    }

    @Test
    public void testJvk12() {
        Ticket ticket = sSmsParser.parse("90230012", "77-362 67-15-28 DPKV a.s. - SMS jizdenka prestupni zlevnena. Cena 12 Kc vc. DPH. Platna od 30.6.2013 21:53 do 30.6.2013 22:53");
        Assert.assertEquals("Karlovy Vary", ticket.city);
        Assert.assertEquals(12, ticket.price, 0);
        Assert.assertEquals("77-362 67-15-28", ticket.hash);
        Assert.assertEquals(new Date("Jun 30, 2013, 21:53:00"), ticket.validFrom);
        Assert.assertEquals(new Date("Jun 30, 2013, 22:53:00"), ticket.validTo);
    }


    // ------ Brno
    @Test
    public void testBrno20() {
        // verified 19.3.2015
        Ticket ticket = sSmsParser.parse("90206020", "DPMB, a.s. Jizdenka prestupni 20 Kc. Plati v zonach 100+101 mimo vlak. Platnost: Od: 19.3.15 14:55 Do: 19.3.15 15:15 YwSyvscVM / 645854");
        Assert.assertEquals("Brno", ticket.city);
        Assert.assertEquals(20, ticket.price, 0);
        Assert.assertEquals("YwSyvscVM / 645854", ticket.hash);
        Assert.assertEquals(new Date("March 19, 2015, 14:55:00"), ticket.validFrom);
        Assert.assertEquals(new Date("March 19, 2015, 15:15:00"), ticket.validTo);
    }

    @Test
    public void testBrno29() {
        // verified 10.4.2015
        Ticket ticket = sSmsParser.parse("90206029", "DPMB, a.s. Jizdenka prestupni 29 Kc. Plati v zonach 100+101 mimo vlak. Platnost: Od: 10.4.15 15:40 Do: 10.4.15 16:55 j3t9Mgjsi / 989865");
        Assert.assertEquals("Brno", ticket.city);
        Assert.assertEquals(29, ticket.price, 0);
        Assert.assertEquals("j3t9Mgjsi / 989865", ticket.hash);
        Assert.assertEquals(new Date("April 10, 2015, 15:40:00"), ticket.validFrom);
        Assert.assertEquals(new Date("April 10, 2015, 16:55:00"), ticket.validTo);
    }

    @Test
    public void testBrnoD() {
        Ticket ticket = sSmsParser.parse("90206099", "DPMB, a.s. Jizdenka prestupni 99 Kc. Plati v zonach 100+101 mimo vlak. Platnost: Od: 22.12.15 7:44 Do: 23.12.15 7:44 dHBAjDfBf / 214315");
        Assert.assertEquals("Brno", ticket.city);
        Assert.assertEquals(99, ticket.price, 0);
        Assert.assertEquals("dHBAjDfBf / 214315", ticket.hash);
        Assert.assertEquals(new Date("December 22, 2015, 07:44:00"), ticket.validFrom);
        Assert.assertEquals(new Date("December 23, 2015, 07:44:00"), ticket.validTo);
    }

    // ------ Sokolov
    @Test
    public void testSod() {
        // verified 19.3.2015
        Ticket ticket = sSmsParser.parse("90206030", "My7 / 370244 Autobusy Karlovy Vary, a.s., MHD Sokolov, jednodenni, 30 Kc, Platnost: od: 3.2.15 4:18 do: 3.2.15 23:59. kxBdLr3f7");
        Assert.assertEquals("Sokolov", ticket.city);
        Assert.assertEquals(30, ticket.price, 0);
        Assert.assertEquals("My7 / 370244", ticket.hash);
        Assert.assertEquals(new Date("February 3, 2015, 4:18:00"), ticket.validFrom);
        Assert.assertEquals(new Date("February 3, 2015, 23:59:00"), ticket.validTo);
    }

    // ----- Tábor
    @Test
    public void testTabor() {
        Ticket ticket = sSmsParser.parse("90206018", "COMETT PLUS, spol. s r. o. Jizdenka plnocenna 18 Kc. Platnost " +
            "od: 19.11.15 8:29 do: 19.11.15 9:39 9QYHzAXWU / 310224");
        Assert.assertEquals("Tábor", ticket.city);
        Assert.assertEquals(18, ticket.price, 0);
        Assert.assertEquals("9QYHzAXWU / 310224", ticket.hash);
        Assert.assertEquals(new Date("November 19, 2015, 8:29:00"), ticket.validFrom);
        Assert.assertEquals(new Date("November 19, 2015, 9:39:00"), ticket.validTo);
    }


    @Test
    public void testTaborZ() {
        Ticket ticket = sSmsParser.parse("90206010", "COMETT PLUS, spol. s r. o. Jizdenka zlevnena 10 Kc. Platnost " +
            "od: 19.11.15 16:52 do: 19.11.15 18:02 adCpmNz8o / 806098");
        Assert.assertEquals("Tábor", ticket.city);
        Assert.assertEquals(10, ticket.price, 0);
        Assert.assertEquals("adCpmNz8o / 806098", ticket.hash);
        Assert.assertEquals(new Date("November 19, 2015, 16:52:00"), ticket.validFrom);
        Assert.assertEquals(new Date("November 19, 2015, 18:02:00"), ticket.validTo);
    }


}
