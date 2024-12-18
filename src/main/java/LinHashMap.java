package main.java;

/************************************************************************************
 * @file LinHashMap.java
 *
 * @author  John Miller
 */

import java.io.*;
import java.lang.reflect.Array;
import static java.lang.System.out;
import java.util.*;

/************************************************************************************
 * This class provides hash maps that use the Linear Hashing algorithm.
 * A hash table is created that is an expandable array-list of buckets.
 */
public class LinHashMap <K, V>
       extends AbstractMap <K, V>
       implements Serializable, Cloneable, Map <K, V>
{
    /** The debug flag
     */
    private static final boolean DEBUG = true;

    /** The number of slots (for key-value pairs) per bucket.
     */
    private static final int SLOTS = 4;

    /** The threshold/upper bound on the load factor
     */
    private static final double THRESHOLD = 1.2;

    /** The class for type K.
     */
    private final Class <K> classK;

    /** The class for type V.
     */
    private final Class <V> classV;

    /********************************************************************************
     * This inner class defines buckets that are stored in the hash table.
     */
    private class Bucket
    {
        int    nKeys;
        K []   key;
        V []   value;
        Bucket next;

        @SuppressWarnings("unchecked")
        Bucket ()
        {
            nKeys = 0;
            key   = (K []) Array.newInstance (classK, SLOTS);
            value = (V []) Array.newInstance (classV, SLOTS);
            next  = null;
        } // constructor

        V find (K k)
        {
            for (var j = 0; j < nKeys; j++) if (key[j].equals (k)) return value[j];
            return null;
        } // find

        void add (K k, V v)
        {
            key[nKeys]   = k;
            value[nKeys] = v;
            nKeys++;
        } // add

        void print ()
        {
            out.print ("[ " );
            for (var j = 0; j < nKeys; j++) out.print (key[j] + " . ");
            out.println ("]" );
        } // print

    } // Bucket inner class

    /** The list of buckets making up the hash table.
     */
    private final List <Bucket> hTable;

    /** The modulus for low resolution hashing
     */
    private int mod1;

    /** The modulus for high resolution hashing
     */
    private int mod2;

    /** The index of the next bucket to split.
     */
    private int isplit = 0;

    /** Counter for the number buckets accessed (for performance testing).
     */
    private int count = 0;

    /** The counter for the total number of keys in the LinHash Map
     */
    private int keyCount = 0;

    /********************************************************************************
     * Construct a hash table that uses Linear Hashing.
     * @param classK  the class for keys (K)
     * @param classV  the class for values (V)
     */
    public LinHashMap (Class <K> _classK, Class <V> _classV)
    {
        classK = _classK;
        classV = _classV;
        mod1   = 4;                                                          // initial size
        mod2   = 2 * mod1;
        hTable = new ArrayList <> ();
        for (var i = 0; i < mod1; i++) hTable.add (new Bucket ());
    } // constructor

    /********************************************************************************
     * Return a set containing all the entries as pairs of keys and values.
     * @return  the set view of the map
     */
    public Set <Entry <K, V>> entrySet ()
    {
        var enSet = new HashSet <Entry <K, V>> ();

        //  T O   B E   I M P L E M E N T E D
        //called on a map object and then inf
        for (Bucket bucket : hTable) { // iterate through each bucket
            for (Bucket b = bucket; b != null; b = b.next) { // iterate through each node of each bucket's list
                for (int i = 0; i < b.nKeys; i++) {
                    enSet.add(new AbstractMap.SimpleEntry<>(b.key[i], b.value[i])); // add entry with key and value of each bucket node
                }
            }
        }
        return enSet;
    } // entrySet

    static class enSetHelper<K,V> implements Entry<K,V> {
        final K ekey;
        final V evalue;

        enSetHelper(K key, V value){
            this.ekey = key;
            this.evalue = value;
        }

        @Override
        public K getKey() {
            return this.ekey;
        }

        @Override
        public V getValue() {
            return this.evalue;
        }

        @Override
        public V setValue(V value) {
            return null;
        }

    }
    /********************************************************************************
     * Given the key, look up the value in the hash table.
     * @param key  the key used for look up
     * @return  the value associated with the key
     */
    @SuppressWarnings("unchecked")
    public V get (Object key)
    {
        var i = findRightBucket (key);
        return find ((K) key, hTable.get (i), true);
    } // get

    /********************************************************************************
     * Put the key-value pair in the hash table.  Split the 'isplit' bucket chain
     * when the load factor is exceeded.
     * @param key    the key to insert
     * @param value  the value to insert
     * @return  the old/previous value, null if none
     */
    public V put (K key, V value)
    {
    
        var i    = findRightBucket (key);                                    // hash to i-th bucket chain
        var bh   = hTable.get (i);                                           // start with home bucket
        var oldV = find (key, bh, false);                             // find old value associated with key
        out.println ("LinearHashMap.put: key = " + key + ", h() = " + i + ", value = " + value);
        
        if (oldV == null) {  // Only increment if this is a new key
            keyCount++;
            var lf = loadFactor ();                                  
            if (lf > THRESHOLD) split();
        }

        var b = bh;
        while (true)  {
            if (b.nKeys < SLOTS) { b.add (key, value); return oldV; }
            if (b.next != null) b = b.next; else break;
        } // while

        var bn = new Bucket ();
        bn.add (key, value);
        b.next = bn;                                                         // add new bucket at end of chain
        return oldV;
    } // put


    /********************************************************************************
     * Print the hash table.
     */
    public void print ()
    {
        out.println ("LinHashMap");
        out.println ("-------------------------------------------");

        for (var i = 0; i < hTable.size (); i++) {
            out.print ("Bucket [ " + i + " ] = ");
            var j = 0;
            for (var b = hTable.get (i); b != null; b = b.next) {
                if (j > 0) out.print (" \t\t --> ");
                b.print ();
                j++;
            } // for
        } // for

        out.println ("-------------------------------------------");
    } // print
 
    /********************************************************************************
     * Return the size (SLOTS * number of home buckets) of the hash table. 
     * @return  the size of the hash table
     */
    public int size ()
    {
        return SLOTS * (mod1 + isplit);
    } // size

    /********************************************************************************
     * Split bucket chain 'isplit' by creating a new bucket chain at the end of the
     * hash table and redistributing the keys according to the high resolution hash
     * function 'h2'.  Increment 'isplit'.  If current split phase is complete,
     * reset 'isplit' to zero, and update the hash functions.
     */
    private void split () {
        int count = 0;
        List<Bucket> bucketCurrent1 = hTable;
        Bucket newOne = new Bucket();

        List<Bucket> hTableCopy = new ArrayList<>(hTable.size());
        // var buckets = entrySet();

        //adds the new bucket to the copy table
        List<Bucket> temp = new ArrayList<>();

        // Iterator<Bucket> iterator = (Iterator<Bucket>) collection.iterator();
        /*
        //Entry<K,V> entry: hTableCopy.get(isplit)
        for (Entry<K,V> entry: buckets){
            if ((h2(entry.getKey()) % 2 == 0){
                bucketCurrent1.put(entry.getKey(), entry.getValue());
                hashfixer.add(entry.getKey);
            }
        }
        */
        if (loadFactor() > THRESHOLD ){
            var entry = bucketCurrent1.get(isplit);
            Bucket bucket1 = new Bucket();
            bucketCurrent1.add(bucket1);
            for (int i = 0; i < entry.nKeys; i++){
                if (h2(entry.key[i]) == isplit) {
                    continue;
                } else {
                    bucket1.add(entry.key[i], entry.value[i]); // add to new bucket
                }
            }
            isplit++;
            if (isplit >= hTable.size()){
                isplit = 0;
            }
        }

        out.println ("split: bucket chain " + isplit);

         /*
        if (hTable.get(isplit).key(3) != null){
            hTable.add(new Bucket());
            keyCount += 1;
            continue;
        } else {
            break;
        }
        Bucket bucketCurrent = hTable.get(isplit);
        int newLoc = 0;
        List<LinHashMap<K, V>.Bucket> newKeys = (List<LinHashMap<K, V>.Bucket>) new ArrayList<K>(0);
        for (int i = 0; i < keyCount; i++) {
            if (hTable.get(i).key[i] != null) {
                newLoc = h2(hTable.get(i).key[i]);
            } else {
                continue;
            }
            if(isplit == newLoc){
                newKeys.add((LinHashMap<K, V>.Bucket) hTable.get(i).key[i]);
            } else {
                for (int j = 0; j < 4; j++){
                    if (hTable.get(i).value[j] != null && hTable.get(i).key[i] != null){
                        hTable.get(newLoc).add(hTable.get(i).key[i], hTable.get(i).value[j]);
                    }
                }
                hTable.get(newLoc).add(hTable.get(i).key[i],hTable.get(i).value[i]);
            }
            newKeys.add(hTable.get(i));
        }
        isplit = (isplit + 1) % keyCount;

        //added this to make sure that the value reset after
        // all the values are incremented through
        */



        //  T O   B E   I M P L E M E N T E D

    } // split

    /********************************************************************************
     * Return the load factor for the hash table.
     * @return  the load factor
     */
    private double loadFactor ()
    {
        return keyCount / (double) size ();
    } // loadFactor

    /********************************************************************************
     * Find the key in the bucket chain that starts with home bucket bh.
     * @param key     the key to find
     * @param bh      the given home bucket
     * @param by_get  whether 'find' is called from 'get' (performance monitored)
     * @return  the current value stored stored for the key
     */
    private V find (K key, Bucket bh, boolean by_get)
    {
        for (var b = bh; b != null; b = b.next) {
            if (by_get) count++;
            V result = b.find (key);
            if (result != null) return result;
        } // for
        return null;
    } // find

    private int findRightBucket(Object key){
        int bucket = h(key);
        if (bucket < isplit) {
            bucket = h2(key);
        }
        return bucket;
    }

    /********************************************************************************
     * Hash the key using the low resolution hash function.
     * @param key  the key to hash
     * @return  the location of the bucket chain containing the key-value pair
     */
    private int h (Object key)
    {
        int ret = key.hashCode () % mod1;
        if(ret < 0) ret += mod1;
        return ret; 
    } // h

    /********************************************************************************
     * Hash the key using the high resolution hash function.
     * @param key  the key to hash
     * @return  the location of the bucket chain containing the key-value pair
     */
    private int h2 (Object key)
    {
        int ret = key.hashCode () % mod2;
        if(ret < 0) ret += mod2;
        return ret; 
    } // h2

    /********************************************************************************
     * The main method used for testing.
     * @param  the command-line arguments (args [0] gives number of keys to insert)
     */
    public static void main (String [] args)
    {
        var totalKeys = 40;
        var RANDOMLY  = false;

        LinHashMap <Integer, Integer> ht = new LinHashMap <> (Integer.class, Integer.class);
        if (args.length == 1) totalKeys = Integer.valueOf (args [0]);

        if (RANDOMLY) {
            var rng = new Random ();
            for (var i = 1; i <= totalKeys; i += 2) ht.put (rng.nextInt (2 * totalKeys), i * i);
        } else {
            for (var i = 1; i <= totalKeys; i += 2) ht.put (i, i * i);
        } // if

        ht.print ();
        for (var i = 0; i <= totalKeys; i++) {
            out.println ("key = " + i + " value = " + ht.get (i));
        } // for
        out.println ("-------------------------------------------");
        out.println ("Average number of buckets accessed = " + ht.count / (double) totalKeys);
    } // main

} // LinHashMap class

