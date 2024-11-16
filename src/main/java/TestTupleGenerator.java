package main.java;
 
/*****************************************************************************************
 * @file  TestTupleGenerator.java
 *
 * @author   Sadiq Charaniya, John Miller
 */

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;

/*****************************************************************************************
 * This class tests the TupleGenerator on the Student Registration Database defined in the
 * Kifer, Bernstein and Lewis 2006 database textbook (see figure 3.6).  The primary keys
 * (see figure 3.6) and foreign keys (see example 3.2.2) are as given in the textbook.
 */
public class TestTupleGenerator
{
    /*************************************************************************************
     * The main method is the driver for TestGenerator.
     * @param args  the command-line arguments
     */
    public static void main (String [] args)
    {
        var test = new TupleGeneratorImpl ();

        test.addRelSchema ("Student",
                           "id name address status",
                           "Integer String String String",
                           "id",
                           null);
        
        test.addRelSchema ("Professor",
                           "id name deptId",
                           "Integer String String",
                           "id",
                           null);
        
        test.addRelSchema ("Course",
                           "crsCode deptId crsName descr",
                           "String String String String",
                           "crsCode",
                           null);
        
        test.addRelSchema ("Teaching",
                           "crsCode semester profId",
                           "String String Integer",
                           "crcCode semester",
                           new String [][] {{ "profId", "Professor", "id" },
                                            { "crsCode", "Course", "crsCode" }});
        
        test.addRelSchema ("Transcript",
                           "studId crsCode semester grade",
                           "Integer String String String",
                           "studId crsCode semester",
                           new String [][] {{ "studId", "Student", "id"},
                                            { "crsCode", "Course", "crsCode" },
                                            { "crsCode semester", "Teaching", "crsCode semester" }});

        var tables = new String [] { "Student", "Professor", "Course", "Teaching", "Transcript" };
        var tups   = new int [] { 10000, 1000, 2000, 50000, 5000 };
        String[][] tableAttributes = {
            {"id name address status"},
            {"id name deptId"},
            {"crsCode deptId crsName descr"},
            {"crsCode semester profId"},
            {"studId crsCode semester grade"}
        };
        Class[][] tableDomains = {
            {Integer.class, String.class, String.class, String.class},
            {Integer.class, String.class, String.class},
            {String.class, String.class, String.class, String.class},
            {String.class, String.class, Integer.class},
            {Integer.class, String.class, String.class, String.class},
        };
        String[][] tableKeys = {
            {"id"},
            {"id"},
            {"crsCode"},
            {"crsCode semester"},
            {"studId crsCode semester"}
        };
    
        var resultTest = test.generate (tups);

        // list containing all tables created
        List<Table> tableObjects = new ArrayList<>();

        for (var i = 0; i < resultTest.length; i++) { // iterate through each table
            // getting table meta data
            String tableName = tables[i];
            String[] attribute = tableAttributes[i][0].split(" ");
            Class[] domain = tableDomains[i];
            String[] key = tableKeys[i];
            // creating list of rows for each table
            List <Comparable []> rows = new ArrayList <> ();
            for (var j = 0; j < resultTest[i].length; j++) {
                Comparable[] tuple = new Comparable[resultTest[i][j].length]; // creating tuple object
                for (var k = 0; k < resultTest[i][j].length; k++) {
                    tuple[k] = resultTest[i][j][k]; // populate tuple with values
                } // for
                rows.add(tuple);
            } // for
            // creating table then adding table to tableObjects
            Table table = new Table(tableName, attribute, domain, key, rows);
            tableObjects.add(table);
        }

        /*
        // for printing tables to verify
        for (Table table: tableObjects) {
            table.print();
        }
        
        // printint tuples to terminal
        for (var i = 0; i < resultTest.length; i++) {
            out.println (tables[i]);
            for (var j = 0; j < resultTest[i].length; j++) {
                for (var k = 0; k < resultTest[i][j].length; k++) {
                    out.print (resultTest[i][j][k] + ",");
                } // for
                out.println ();
            } // for
            out.println ();
        } // for
        */
        /*                                                                                                                                                                                                                                                              
        // for printing tables to verify                                                                                                                                                                                                                                
        for (Table table: tableObjects) {                                                                                                                                                                                                                               
            table.print();                                                                                                                                                                                                                                              
        }                                                                                                                                                                                                                                                               
                                                                                                                                                                                                                                                                        
        // printing tuples to terminal                                                                                                                                                                                                                                  
        for (var i = 0; i < resultTest.length; i++) {                                                                                                                                                                                                                   
            out.println (tables[i]);                                                                                                                                                                                                                                    
            for (var j = 0; j < resultTest[i].length; j++) {                                                                                                                                                                                                            
                for (var k = 0; k < resultTest[i][j].length; k++) {                                                                                                                                                                                                     
                    out.print (resultTest[i][j][k] + ",");                                                                                                                                                                                                              
                } // for                                                                                                                                                                                                                                                
                out.println ();                                                                                                                                                                                                                                         
            } // for                                                                                                                                                                                                                                                    
            out.println ();                                                                                                                                                                                                                                             
        } // for                                                                                                                                                                                                                                                        
        */

        Table studentTable = tableObjects.get(0);
        Table transcriptTable = tableObjects.get(4);

        int[] tupleCounts = {500, 1000, 2000, 5000, 10000};
        String attributeToSelect = "id";
        int selectValue = 123; // Example value to search                                                                                                                                                                                                               

        // Loop through different indexing mechanisms                                                                                                                                                                                                                   
        Map<String, Map<KeyType, Comparable[]>> indexTypes = new HashMap<>();
        indexTypes.put("NoIndex", null); // No indexing                                                                                                                                                                                                                 
        indexTypes.put("TreeMap", new TreeMap<>());
        indexTypes.put("HashMap", new HashMap<>());
        indexTypes.put("LinHashMap", new LinHashMap<>(KeyType.class, Comparable[].class));

        for (String indexType : indexTypes.keySet()) {
            Map<KeyType, Comparable[]> index = indexTypes.get(indexType);

           //       for (int tupleCount : tupleCounts) {                                                                                                                                                                                                                
            //          System.out.println("\n--- Testing " + indexType + " with " + tupleCount + " tuples ---");                                                                                                                                                       
            System.out.println("\nTesting with index type: " + indexType);

                // Measure time for SELECT operation                                                                                                                                                                                                                    
                long startSelect = System.currentTimeMillis();
                Table resultSelect = studentTable.select(t -> t[studentTable.col("id")].equals(selectValue));
                long endSelect = System.currentTimeMillis();
                System.out.println("Select operation took: " + (endSelect - startSelect) + " ms");

                // Measure time for JOIN operation                                                                                                                                                                                                                      
                long startJoin = System.currentTimeMillis();
                Table resultJoin = studentTable.join("id", "studId", transcriptTable);
                long endJoin = System.currentTimeMillis();
                System.out.println("Join operation took: " + (endJoin - startJoin) + " ms");
                //          }                                                                                                                                                                                                                                           
        }
    } // main

} // TestTupleGenerator

