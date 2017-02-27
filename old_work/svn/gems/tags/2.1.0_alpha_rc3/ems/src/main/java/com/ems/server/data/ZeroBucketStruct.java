package com.ems.server.data;

import java.util.Date;

  public  class ZeroBucketStruct {

        long fixtureId;
        Date lastStatsRcvdTime;
        Date latestStateRcvdTime;

        public ZeroBucketStruct(long fixtureId, Date lastStatsRcvdTime, Date latestStatsRcvdTime) {

            this.fixtureId = fixtureId;
            this.lastStatsRcvdTime = lastStatsRcvdTime;
            this.latestStateRcvdTime = latestStatsRcvdTime;

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
        
        

    } // end of class MissingBucketStruct