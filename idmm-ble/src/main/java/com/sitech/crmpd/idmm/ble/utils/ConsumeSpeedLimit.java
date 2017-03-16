package com.sitech.crmpd.idmm.ble.utils;

/**
 * Created by guanyf on 2016/7/25.
 */
public final class ConsumeSpeedLimit {
    // Consume Speed Limit (csl)
    private int csl_maxfetchPM = 100; //consume speed limit,  n Per Minute,  <= 0 for no limit
    private final int[] csl_counters = new int[6]; //speed counter, 10s per element, item 0 is the recent.
    private long csl_time; // the time that last move the counters

    public ConsumeSpeedLimit(int limit) {
        this.csl_maxfetchPM = limit;
    }
    /**
     * check if speed is exceed the limit
     * @return true if allow the fetch
     */
    public final boolean check() {
        if(csl_maxfetchPM <= 0)
            return true;
        long cur = System.currentTimeMillis();
        long x = ( cur - csl_time ) / 10000;
        if( x > csl_counters.length)
            x = csl_counters.length;
        System.out.println("check x="+x);
        if ( x > 0) { // time unit past, move the array
            csl_time = cur;
            for (int i = csl_counters.length - 1; i >= x; i--)
                csl_counters[i] = csl_counters[i - 1];

            for (int i = 0; i < x; i++)
                csl_counters[i] = 0;
        }

        int sum = 0;
        for(int i: csl_counters) sum += i;
        System.out.println("sum:"+sum);
        return sum < csl_maxfetchPM;
    }
    public final void add() {
        if(csl_maxfetchPM <= 0)
            return;
        long cur = System.currentTimeMillis();
        long x = ( cur - csl_time ) / 10000;
        if( x > csl_counters.length)
            x = csl_counters.length;
        else if(x == 0){
            csl_counters[0] += 1;
            return;
        }
        csl_time = cur;
        for(int i = csl_counters.length-1; i >= x; i--)
            csl_counters[i] = csl_counters[i-1];

        for(int i=0; i<x; i++)
            csl_counters[i] = 0;
        csl_counters[0] += 1;
    }

    public int setLimit(int v) {
        int old_limit = csl_maxfetchPM;
        csl_maxfetchPM = v;
        return old_limit;
    }

    public static void main(String[] args) throws Exception{
        ConsumeSpeedLimit c = new ConsumeSpeedLimit(100);
        for(int i=0; i<100; i++) c.add();
        System.out.println("check  return:" + c.check());

        long cur = System.currentTimeMillis();
        boolean r;
        while( !(r = c.check() )) {
            System.out.println("check  return:" + r + "  " + ((System.currentTimeMillis() - cur) / 1000));
            Thread.sleep(10 * 1000);
        }
    }

    public int getLimit() {
        return csl_maxfetchPM;
    }

}
