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

public class SmsParserSlovakTest {

    static SmsParser sSmsParser;

    @BeforeClass
    public static void setup() {
        sSmsParser = new SmsParser("svk");
        // "ces" = app in Czech language
        // "" = app in English language
    }

    // ------ Bratislava
    @Test
    public void test1100() {
        Ticket ticket = sSmsParser.parse("1100", "DPB, a.s. Prestupny CL 1,30EUR Platnost od 24-08-2011 18:42 do 19:52 hod. gwyywg4u6bh");
        Assert.assertEquals("Bratislava", ticket.city);
        Assert.assertEquals(1.30, ticket.price, 0);
        Assert.assertEquals("gwyywg4u6bh", ticket.hash);
        Assert.assertEquals(new Date("August 24, 2011, 18:42:00"), ticket.validFrom);
        Assert.assertEquals(new Date("August 24, 2011, 19:52:00"), ticket.validTo);
    }

    @Test
    public void test1104() {
        Ticket ticket = sSmsParser.parse("1140", "DPB, a.s. Prestupny CL 1,00EUR Platnost od 24-08-2011 18:42 do 19:52 hod. gwyywg4u6bh");
        Assert.assertEquals("Bratislava", ticket.city);
        Assert.assertEquals(1, ticket.price, 0);
        Assert.assertEquals("gwyywg4u6bh", ticket.hash);
        Assert.assertEquals(new Date("August 24, 2011, 18:42:00"), ticket.validFrom);
        Assert.assertEquals(new Date("August 24, 2011, 19:52:00"), ticket.validTo);
    }

    @Test
    public void test1124() {
        Ticket ticket = sSmsParser.parse("1124", "DPB, a.s. Prestupny 24 hod CL 3,50EUR Platnost od 26-11-2009 18:55 do 27-11-2009 18:55 hod. nhn1knmtytx");
        Assert.assertEquals("Bratislava", ticket.city);
        Assert.assertEquals(4.50, ticket.price, 0.0);
        Assert.assertEquals("nhn1knmtytx", ticket.hash);
        Assert.assertEquals(new Date("November 26, 2009, 18:55:00"), ticket.validFrom);
        Assert.assertEquals(new Date("November 27, 2009, 18:55:00"), ticket.validTo);
    }

    // ------ Kosice
    @Test
    public void test1166() {
        Ticket ticket = sSmsParser.parse("1166", "DPMK, a.s. SMS prestupny CL 0,80 EUR Platnost od 31-08-2011 13:45 do 14:45 hod. 72n42p9e");
        Assert.assertEquals("Košice", ticket.city);
        Assert.assertEquals(0.8, ticket.price, 0.0);
        Assert.assertEquals("72n42p9e", ticket.hash);
        Assert.assertEquals(new Date("August 31, 2011, 13:45:00"), ticket.validFrom);
        Assert.assertEquals(new Date("August 31, 2011, 14:45:00"), ticket.validTo);
    }

    // ------ Zilina
    @Test
    public void test1155() {
        Ticket ticket = sSmsParser.parse("1155", "DPMZ, s.r.o. Prestupny CL 0,80EUR (24,10Sk) 1EUR=30,1260Sk Platnost od 31-08-2011 13:44 do 14:44 hod. omqobbfo6sn");
        Assert.assertEquals("Žilina", ticket.city);
        Assert.assertEquals(0.8, ticket.price, 0.0);
        Assert.assertEquals("omqobbfo6sn", ticket.hash);
        Assert.assertEquals(new Date("August 31, 2011, 13:44:00"), ticket.validFrom);
        Assert.assertEquals(new Date("August 31, 2011, 14:44:00"), ticket.validTo);
    }

    // ------ Presov
    @Test
    public void test1144() {
        Ticket ticket = sSmsParser.parse("1144", "DPMP, a.s. SMS prestupny CL 0,70 EUR Platnost od 08.04.2013 16:26 do 16:56 hod. 393hg7h1");
        Assert.assertEquals("Prešov", ticket.city);
        Assert.assertEquals(0.7, ticket.price, 0.0);
        Assert.assertEquals("393hg7h1", ticket.hash);
        Assert.assertEquals(new Date("April 8, 2013, 16:26:00"), ticket.validFrom);
        Assert.assertEquals(new Date("April 8, 2013, 16:56:00"), ticket.validTo);
    }

}
