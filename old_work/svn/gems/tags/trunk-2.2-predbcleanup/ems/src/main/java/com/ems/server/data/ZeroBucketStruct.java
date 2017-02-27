package com.ems.server.data;

import java.util.Date;

  public  class ZeroBucketStruct {

        long fixtureId;
        Date lastStatsRcvdTime;
        Date latestStateRcvdTime;
        boolean sweepEnabled;

        public ZeroBucketStruct(long fixtureId, Date lastStatsRcvdTime, Date latestStatsRcvdTime,
            boolean sweepEnabled) {

            this.fixtureId = fixtureId;
            this.lastStatsRcvdTime = lastStatsRcvdTime;
            this.latestStateRcvdTime = latestStatsRcvdTime;
            this.sweepEnabled = sweepEnabled;

        } // end of constructor

		public long getFixtureId() {
			return fixtureId;
		}

		public Date getLastStatsRcvdTime() {
			return lastStatsRcvdTime;
		}

		public Date getLatestStateRcvdTime() {
			return latestStateRcvdTime;
		}
        
		public boolean isSweepEnabled() {
		  return sweepEnabled;
		}

    } // end of class MissingBucketStruct