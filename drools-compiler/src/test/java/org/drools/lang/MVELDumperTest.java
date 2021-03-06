package org.drools.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.drools.base.evaluators.MatchesEvaluatorsDefinition;
import org.drools.base.evaluators.SetEvaluatorsDefinition;
import org.drools.compiler.DrlExprParser;
import org.drools.lang.MVELDumper.MVELDumperContext;
import org.drools.lang.descr.AtomicExprDescr;
import org.drools.lang.descr.BindingDescr;
import org.drools.lang.descr.ConstraintConnectiveDescr;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.builder.conf.LanguageLevelOption;

public class MVELDumperTest {

    private MVELDumper dumper;

    @Before
    public void setUp() throws Exception {
        // configure operators
        new SetEvaluatorsDefinition();
        new MatchesEvaluatorsDefinition();

        dumper = new MVELDumper();
    }

    @Test
    public void testDump() throws Exception {
        String input = "price > 10 && < 20 || == $val || == 30";
        String expected = "( price > 10 && price < 20 || price == $val || price == 30 )";

        ConstraintConnectiveDescr descr = parse( input );
        String result = dumper.dump( descr );

        assertEquals( expected,
                      result );
    }

    @Test
    public void testDumpMatches() throws Exception {
        String input = "type.toString matches \"something\\swith\\tsingle escapes\"";
        String expected = "type.toString ~= \"something\\swith\\tsingle escapes\"";

        ConstraintConnectiveDescr descr = parse( input );
        String result = dumper.dump( descr );

        assertEquals( expected,
                      result );
    }

    @Test
    public void testDumpMatches2() throws Exception {
        String input = "type.toString matches 'something\\swith\\tsingle escapes'";
        String expected = "type.toString ~= \"something\\swith\\tsingle escapes\"";

        ConstraintConnectiveDescr descr = parse( input );
        String result = dumper.dump( descr );

        assertEquals( expected,
                      result );
    }

    @Test
    public void testDumpMatches3() throws Exception {
        String input = "this[\"content\"] matches \"hello ;=\"";
        String expected = "this[\"content\"] ~= \"hello ;=\"";

        ConstraintConnectiveDescr descr = parse( input );
        String result = dumper.dump( descr );

        assertEquals( expected,
                      result );
    }

    @Test
    public void testDumpContains() throws Exception {
        String input = "list contains \"b\"";
        String expected = "list contains \"b\"";

        ConstraintConnectiveDescr descr = parse( input );
        String result = dumper.dump( descr );

        assertEquals( expected,
                      result );
    }

    @Test
    public void testDumpContains2() throws Exception {
        String input = "list not contains \"b\"";
        String expected = "!( list contains \"b\" )";

        ConstraintConnectiveDescr descr = parse( input );
        String result = dumper.dump( descr );

        assertEquals( expected,
                      result );
    }

    @Test
    public void testDumpExcludes() throws Exception {
        String input = "list excludes \"b\"";
        String expected = "!( list contains \"b\" )";

        ConstraintConnectiveDescr descr = parse( input );
        String result = dumper.dump( descr );

        assertEquals( expected,
                      result );
    }

    @Test
    public void testDumpExcludes2() throws Exception {
        String input = "list not excludes \"b\"";
        String expected = "list contains \"b\"";

        ConstraintConnectiveDescr descr = parse( input );
        String result = dumper.dump( descr );

        assertEquals( expected,
                      result );
    }

    @Test @Ignore
    public void testDumpWithDateAttr() throws Exception {
        String input = "son.birthDate == \"01-jan-2000\"";
        String expected = "son.birthDate == org.kie.util.DateUtils.parseDate( \"01-jan-2000\" )";

        ConstraintConnectiveDescr descr = parse( input );
        String result = dumper.dump( descr );

        assertEquals( expected,
                      result );
    }

    @Test
    public void testDumpComplex() throws Exception {
        String input = "a ( > 60 && < 70 ) || ( > 50 && < 55 ) && a3 == \"black\" || a == 40 && a3 == \"pink\" || a == 12 && a3 == \"yellow\" || a3 == \"blue\"";
        String expected = "( ( a > 60 && a < 70 || a > 50 && a < 55 ) && a3 == \"black\" || a == 40 && a3 == \"pink\" || a == 12 && a3 == \"yellow\" || a3 == \"blue\" )";

        ConstraintConnectiveDescr descr = parse( input );
        String result = dumper.dump( descr );

        assertEquals( expected,
                      result );
    }

    @Test
    public void testDumpBindings() throws Exception {
        String input = "$x : property > value";
        String expected = "property > value";

        ConstraintConnectiveDescr descr = parse( input );
        MVELDumperContext ctx = new MVELDumperContext();
        String result = dumper.dump( descr,
                                     ctx );

        assertEquals( expected,
                      result );
        assertEquals( 1,
                      ctx.getBindings().size() );
        BindingDescr bind = ctx.getBindings().get( 0 );
        assertEquals( "$x",
                      bind.getVariable() );
        assertEquals( "property",
                      bind.getExpression() );
    }

    @Test
    public void testDumpBindings2() throws Exception {
        String input = "( $a : a > $b : b[10].prop || 10 != 20 ) && $x : someMethod(10) == 20";
        String expected = "( a > b[10].prop || 10 != 20 ) && someMethod(10) == 20";

        ConstraintConnectiveDescr descr = parse( input );
        MVELDumperContext ctx = new MVELDumperContext();
        String result = dumper.dump( descr, 
                                     ctx );

        assertEquals( expected,
                      result );
        assertEquals( 3,
                      ctx.getBindings().size() );
        BindingDescr bind = ctx.getBindings().get( 0 );
        assertEquals( "$a",
                      bind.getVariable() );
        assertEquals( "a",
                      bind.getExpression() );
        bind = ctx.getBindings().get( 1 );
        assertEquals( "$b",
                      bind.getVariable() );
        assertEquals( "b[10].prop",
                      bind.getExpression() );
        bind = ctx.getBindings().get( 2 );
        assertEquals( "$x",
                      bind.getVariable() );
        assertEquals( "someMethod(10)",
                      bind.getExpression() );
    }

    @Test
    public void testDumpBindings3() throws Exception {
        String input = "( $a : a > $b : b[10].prop || 10 != 20 ) && $x : someMethod(10)";
        String expected = "( a > b[10].prop || 10 != 20 )";

        ConstraintConnectiveDescr descr = parse( input );
        String result = dumper.dump( descr );

        assertEquals( expected,
                      result );
    }

    @Test
    public void testDumpBindings4() throws Exception {
        String input = "( $a : a > $b : b[10].prop || $x : someMethod(10) ) && 10 != 20";
        String expected = "( a > b[10].prop ) && 10 != 20";

        ConstraintConnectiveDescr descr = parse( input );
        String result = dumper.dump( descr );

        assertEquals( expected,
                      result );
    }

    @Test
    public void testDumpBindingsWithRestriction() throws Exception {
        String input = "$x : age > 10 && < 20 || > 30";
        String expected = "( age > 10 && age < 20 || age > 30 )";

        ConstraintConnectiveDescr descr = parse( input );
        MVELDumperContext ctx = new MVELDumperContext();
        String result = dumper.dump( descr,
                                     ctx );

        assertEquals( expected,
                      result );
        assertEquals( 1,
                      ctx.getBindings().size() );
        BindingDescr bind = ctx.getBindings().get( 0 );
        assertEquals( "$x",
                      bind.getVariable() );
        assertEquals( "age",
                      bind.getExpression() );
    }

    @Test
    public void testDumpBindingsComplexOp() throws Exception {
        String input = "$x : age in (10, 20, $someVal)";
        String expected = "( age == 10 || age == 20 || age == $someVal )";

        ConstraintConnectiveDescr descr = parse( input );
        MVELDumperContext ctx = new MVELDumperContext();
        String result = dumper.dump( descr,
                                     ctx );

        assertEquals( expected,
                      result );
        assertEquals( 1,
                      ctx.getBindings().size() );
        BindingDescr bind = ctx.getBindings().get( 0 );
        assertEquals( "$x",
                      bind.getVariable() );
        assertEquals( "age",
                      bind.getExpression() );
    }

    @Test
    public void testDumpBindingsComplexOp2() throws Exception {
        String input = "$x : age not in (10, 20, $someVal)";
        String expected = "age != 10 && age != 20 && age != $someVal";

        ConstraintConnectiveDescr descr = parse( input );
        MVELDumperContext ctx = new MVELDumperContext();
        String result = dumper.dump( descr,
                                     ctx );

        assertEquals( expected,
                      result );
        assertEquals( 1,
                      ctx.getBindings().size() );
        BindingDescr bind = ctx.getBindings().get( 0 );
        assertEquals( "$x",
                      bind.getVariable() );
        assertEquals( "age",
                      bind.getExpression() );
    }

    @Test
    public void testProcessInlineCast() throws Exception {
        String expr = "field1#Class.field2";
        String expectedInstanceof = "field1 instanceof Class && ";
        String expectedcasted = "((Class)field1).field2";
        AtomicExprDescr atomicExpr = new AtomicExprDescr(expr);
        String[] instanceofAndCastedExpr = dumper.processImplicitConstraints(expr, atomicExpr, null);
        assertEquals(expectedInstanceof, instanceofAndCastedExpr[0]);
        assertEquals(expectedcasted, instanceofAndCastedExpr[1]);
        assertEquals(expectedcasted, atomicExpr.getRewrittenExpression());

        expr = "field1#Class1.field2#Class2.field3";
        expectedInstanceof = "field1 instanceof Class1 && ((Class1)field1).field2 instanceof Class2 && ";
        expectedcasted = "((Class2)((Class1)field1).field2).field3";
        atomicExpr = new AtomicExprDescr(expr);
        instanceofAndCastedExpr = dumper.processImplicitConstraints(expr, atomicExpr, null);
        assertEquals(expectedInstanceof, instanceofAndCastedExpr[0]);
        assertEquals(expectedcasted, instanceofAndCastedExpr[1]);
        assertEquals(expectedcasted, atomicExpr.getRewrittenExpression());
    }

    @Test
    public void testProcessNullSafeDereferencing() throws Exception {
        String expr = "field1!.field2";
        String expectedNullCheck = "field1 != null && ";
        String expectedExpr = "field1.field2";
        AtomicExprDescr atomicExpr = new AtomicExprDescr(expr);
        String[] nullCheckAndExpr = dumper.processImplicitConstraints( expr, atomicExpr, null );
        assertEquals(expectedNullCheck, nullCheckAndExpr[0]);
        assertEquals(expectedExpr, nullCheckAndExpr[1]);
        assertEquals(expectedExpr, atomicExpr.getRewrittenExpression());

        expr = "field1!.field2!.field3";
        expectedNullCheck = "field1 != null && field1.field2 != null && ";
        expectedExpr = "field1.field2.field3";
        atomicExpr = new AtomicExprDescr(expr);
        nullCheckAndExpr = dumper.processImplicitConstraints( expr, atomicExpr, null );
        assertEquals(expectedNullCheck, nullCheckAndExpr[0]);
        assertEquals(expectedExpr, nullCheckAndExpr[1]);
        assertEquals(expectedExpr, atomicExpr.getRewrittenExpression());
    }

    @Test
    public void testProcessImplicitConstraints() throws Exception {
        String expr = "field1#Class!.field2";
        String expectedConstraints = "field1 instanceof Class && ";
        String expectedExpr = "((Class)field1).field2";
        AtomicExprDescr atomicExpr = new AtomicExprDescr(expr);
        String[] constraintsAndExpr = dumper.processImplicitConstraints( expr, atomicExpr, null );
        assertEquals(expectedConstraints, constraintsAndExpr[0]);
        assertEquals(expectedExpr, constraintsAndExpr[1]);
        assertEquals(expectedExpr, atomicExpr.getRewrittenExpression());

        expr = "field1!.field2#Class.field3";
        expectedConstraints = "field1 != null && field1.field2 instanceof Class && ";
        expectedExpr = "((Class)field1.field2).field3";
        atomicExpr = new AtomicExprDescr(expr);
        constraintsAndExpr = dumper.processImplicitConstraints( expr, atomicExpr, null );
        assertEquals(expectedConstraints, constraintsAndExpr[0]);
        assertEquals(expectedExpr, constraintsAndExpr[1]);
        assertEquals(expectedExpr, atomicExpr.getRewrittenExpression());

        expr = "field1#Class.field2!.field3";
        expectedConstraints = "field1 instanceof Class && ((Class)field1).field2 != null && ";
        expectedExpr = "((Class)field1).field2.field3";
        atomicExpr = new AtomicExprDescr(expr);
        constraintsAndExpr = dumper.processImplicitConstraints( expr, atomicExpr, null );
        assertEquals(expectedConstraints, constraintsAndExpr[0]);
        assertEquals(expectedExpr, constraintsAndExpr[1]);
        assertEquals(expectedExpr, atomicExpr.getRewrittenExpression());
    }

    public ConstraintConnectiveDescr parse( final String constraint ) {
        DrlExprParser parser = new DrlExprParser(LanguageLevelOption.DRL5);
        ConstraintConnectiveDescr result = parser.parse( constraint );
        assertFalse( parser.getErrors().toString(),
                     parser.hasErrors() );

        return result;
    }
}
