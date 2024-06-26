/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved.
 *
 * This file is part of the QuickFIX FIX Engine
 *
 * This file may be distributed under the terms of the quickfixengine.org
 * license as defined by quickfixengine.org and appearing in the file
 * LICENSE included in the packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.quickfixengine.org/LICENSE for licensing information.
 *
 * Contact ask@quickfixengine.org if any conditions of this licensing
 * are not clear to you.
 ******************************************************************************/

package quickfix;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;

import org.junit.Test;
import org.quickfixj.CharsetSupport;

import quickfix.field.ClOrdID;
import quickfix.field.ExecInst;
import quickfix.field.MDUpdateAction;
import quickfix.field.OrdType;
import quickfix.field.RawData;
import quickfix.field.Side;
import quickfix.field.TradeCondition;
import quickfix.field.TransactTime;
import quickfix.fix50.MarketDataIncrementalRefresh;
import quickfix.fix50.NewOrderSingle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FieldTest {

    @Test
    public void testMessageSetGetString() {
        Side side1 = new Side(Side.BUY);
        Side side2 = new Side(Side.BUY);
        assertEquals(side1, side2);

        side2 = new Side();
        side2.setValue(Side.BUY);
        assertEquals(side1, side2);
    }

    private void testFieldCalcuations(String value, int checksum, int length) {
        Field<String> field = new Field<>(12, value);
        field.setObject(value);
        assertEquals("12=" + value, field.toString());
        assertEquals(checksum, field.getChecksum());
        assertEquals(length, field.getLength());

        value = value.substring(0, value.length() - 1) + (char)(value.charAt(value.length() - 1) + 1);
        checksum = (checksum + 1) & 0xFF;
        field.setObject(value);
        assertEquals("12=" + value, field.toString());
        assertEquals(checksum, field.getChecksum());
        assertEquals(length, field.getLength());

        field.setTag(13);
        checksum = (checksum + 1) & 0xFF;
        assertEquals("13=" + value, field.toString());
        assertEquals(checksum, field.getChecksum());
        assertEquals(length, field.getLength());
    }

    @Test
    public void testFieldCalculationsWithDefaultCharset() {
        testFieldCalcuations("VALUE", 30, 9);
    }

    @Test
    public void testFieldCalculationsWithUTF8Charset() throws UnsupportedEncodingException {
        CharsetSupport.setCharset("UTF-8");
        try {
            testFieldCalcuations("\u6D4B\u9A8C\u6570\u636E", 50, 16);
        } finally {
            CharsetSupport.setDefaultCharset();
        }
    }

    @Test
    public void testDateField() {
        DateField field = new DateField(11);
        Date date = new Date();
        field.setValue(date);
        assertEquals(11, field.getTag());
        assertEquals(date, field.getValue());
        field = new DateField(11, date);
        assertEquals(11, field.getTag());
        assertEquals(date, field.getValue());
    }

    @Test
    public void testUtcDateOnlyField() {
        UtcDateOnlyField field = new UtcDateOnlyField(11);
        LocalDate date = LocalDate.now();
        field.setValue(date);
        assertEquals(11, field.getTag());
        assertEquals(date, field.getValue());
        field = new UtcDateOnlyField(11, date);
        assertEquals(11, field.getTag());
        assertEquals(date, field.getValue());
    }

    @Test
    public void testUtcTimeOnlyField() {
        UtcTimeOnlyField field = new UtcTimeOnlyField(11);
        LocalTime date = LocalTime.now();
        field.setValue(date);
        assertEquals(11, field.getTag());
        assertEquals(date, field.getValue());
        field = new UtcTimeOnlyField(11, date);
        assertEquals(11, field.getTag());
        assertEquals(date, field.getValue());
    }

    @Test
    public void testUtcTimeStampField() {
        UtcTimeStampField field = new UtcTimeStampField(11);
        LocalDateTime date = LocalDateTime.now();
        field.setValue(date);
        assertEquals(11, field.getTag());
        assertEquals(date, field.getValue());
        field = new UtcTimeStampField(11, date);
        assertEquals(11, field.getTag());
        assertEquals(date, field.getValue());
    }

    @Test
    public void testBooleanField() {
        BooleanField field = new BooleanField(11);
        field.setValue(true);
        assertEquals(11, field.getTag());
        assertTrue(field.getValue());
        field.setValue(Boolean.FALSE);
        assertEquals(11, field.getTag());
        assertFalse(field.getValue());
        field = new BooleanField(22, true);
        assertEquals(22, field.getTag());
        assertTrue(field.getValue());
        field = new BooleanField(33, Boolean.TRUE);
        assertEquals(33, field.getTag());
        assertTrue(field.getValue());
    }

    @Test
    public void testDoubleField() {
        DoubleField field = new DoubleField(11);
        field.setValue(12.3);
        assertEquals(11, field.getTag());
        assertEquals(12.3, field.getValue(), 0);
        field.setValue(new Double(23.4));
        assertEquals(11, field.getTag());
        assertEquals(23.4, field.getValue(), 0);
        field = new DoubleField(22, 34.5);
        assertEquals(22, field.getTag());
        assertEquals(34.5, field.getValue(), 0);
        field = new DoubleField(33, new Double(45.6));
        assertEquals(33, field.getTag());
        assertEquals(45.6, field.getValue(), 0);
    }

    @Test(expected = NumberFormatException.class)
    public void testDoubleFieldException() {
        DoubleField field = new DoubleField(11, Double.NaN);
    }

    @Test
    public void testDecimalField() {
        DecimalField field = new DecimalField(11);
        field.setValue(12.3);
        assertEquals(11, field.getTag());
        assertEquals(BigDecimal.valueOf(12.3), field.getValue());
        field.setValue(23.4);
        assertEquals(11, field.getTag());
        assertEquals(BigDecimal.valueOf(23.4), field.getValue());
        field = new DecimalField(22, 34.5);
        assertEquals(22, field.getTag());
        assertEquals(BigDecimal.valueOf(34.5), field.getValue());
        field = new DecimalField(33, 45.6);
        assertEquals(33, field.getTag());
        assertEquals(BigDecimal.valueOf(45.6), field.getValue());
    }

    @Test(expected = NumberFormatException.class)
    public void testDecimalFieldException() {
        DecimalField field = new DecimalField(11, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testCharField() {
        CharField field = new CharField(11);
        field.setValue('x');
        assertEquals(11, field.getTag());
        assertEquals('x', field.getValue());
        field.setValue(Character.valueOf('X'));
        assertEquals(11, field.getTag());
        assertEquals('X', field.getValue());
        field = new CharField(22, 'a');
        assertEquals(22, field.getTag());
        assertEquals('a', field.getValue());
        field = new CharField(33, Character.valueOf('A'));
        assertEquals(33, field.getTag());
        assertEquals('A', field.getValue());
    }

    @Test
    public void testIntField() {
        IntField field = new IntField(11);
        field.setValue(12);
        assertEquals(11, field.getTag());
        assertEquals(12, field.getValue());
        field.setValue(Integer.valueOf(23));
        assertEquals(11, field.getTag());
        assertEquals(23, field.getValue());
        field = new IntField(22, 23);
        assertEquals(22, field.getTag());
        assertEquals(23, field.getValue());
        field = new IntField(33, Integer.valueOf(44));
        assertEquals(33, field.getTag());
        assertEquals(44, field.getValue());
    }

    @Test
    public void testBytesField() {
        byte[] data = "rawdata".getBytes();

        BytesField field = new BytesField(RawData.FIELD);
        field.setValue(data);
        assertEquals(RawData.FIELD, field.getTag());
        assertTrue(Arrays.equals(data, field.getObject()));

        StringBuilder sb = new StringBuilder();
        field.toString(sb);
        assertEquals("96=rawdata", sb.toString());
    }

    @Test
    public void testBytesFieldFullRange() {
        byte[] data = new byte[256];

        for (int i = 0; i < 256; i++) {
            data[i] = (byte) i;
        }

        BytesField field = new BytesField(RawData.FIELD);
        field.setValue(data);

        byte[] tagPrefixedData = field.toString().getBytes(CharsetSupport.getCharsetInstance());
        assertEquals(256 + 3, tagPrefixedData.length);

        for (int i = 0; i < data.length; ++i) {
            assertEquals(data[i], tagPrefixedData[i + 3]);
        }
    }

    // QFJ-881
    @Test
    public void testMultipleStringValue() throws Exception {
        assertEquals(FieldType.MULTIPLESTRINGVALUE, FieldType.fromName("notused", "MULTIPLESTRINGVALUE"));
        assertEquals(FieldType.MULTIPLEVALUESTRING, FieldType.fromName("notused", "MULTIPLEVALUESTRING"));

        MarketDataIncrementalRefresh md = new MarketDataIncrementalRefresh();
        MarketDataIncrementalRefresh.NoMDEntries value = new MarketDataIncrementalRefresh.NoMDEntries();
        value.set(new MDUpdateAction(MDUpdateAction.NEW));
        value.set(new TradeCondition("A B AF AG"));
        md.addGroup(value);

        DataDictionary dd = new DataDictionary("FIX50.xml");
        dd.validate(md, new ValidationSettings());
    }

    @Test
    public void testMultipleCharValue() throws Exception {
        assertEquals(FieldType.MULTIPLECHARVALUE, FieldType.fromName("notused", "MULTIPLECHARVALUE"));

        NewOrderSingle nos = new NewOrderSingle();
        nos.set(new ClOrdID("ORDER-1"));
        nos.set(new Side(Side.BUY));
        nos.set(new OrdType(OrdType.MARKET));
        nos.set(new ExecInst("i V 7"));
        nos.set(new TransactTime(LocalDateTime.of(2020, 3, 10, 12, 23, 44)));

        DataDictionary dd = new DataDictionary("FIX50.xml");
        dd.validate(nos, new ValidationSettings());
    }

    private void assertEqualsAndHash(Field<?> field1, Field<?> field2) {
        assertEquals("fields not equal", field1, field2);
        assertEquals("fields hashcode not equal", field1.hashCode(), field2.hashCode());
    }
}
