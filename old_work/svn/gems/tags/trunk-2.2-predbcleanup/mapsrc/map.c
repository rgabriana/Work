/* file: map.c - program to create a map from nodes
*
* use: map <fileName>
*   where <fileName> is the name of a file
*    with the following format of content:
*      <nodeTable> - contains lines of nodeID, x, y
*      <blankLine>
*      <distanceTable> - contains lines of nodeID, nodeID, distance
*
* dperkins - 24-mar-2010
*/

#include <stdio.h>
#include <stdarg.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <math.h>


unsigned debugFlags;
#define DBCOMPUTELOCS 0x0001
#define DBcomputeLocs (debugFlags & DBCOMPUTELOCS)

#define DBCOMPUTELOCSERR 0x0002
#define DBcomputeLocsErr (debugFlags & DBCOMPUTELOCSERR)

#define DBCOMPUTELOCSPOS 0x0004
#define DBcomputeLocsPos (debugFlags & DBCOMPUTELOCSPOS)

#define DBCOMPUTELOCSGRIDDELTA 0x0008
#define DBcomputeLocsGridDelta (debugFlags & DBCOMPUTELOCSGRIDDELTA)

#define DBADD2QUEUE 0x0010
#define DBadd2Queue (debugFlags & DBADD2QUEUE)

#define DBCOMPUTLOCSR 0x0020
#define DBcomputLocsR (debugFlags & DBCOMPUTLOCSR)

#define DBADDREVDIST 0x0040
#define DBaddRevDist (debugFlags & DBADDREVDIST)

#define DBFILESTATS 0x0080
#define DBfileStats (debugFlags & DBFILESTATS)

#define DBNODESTATS 0x0100
#define DBnodeStats (debugFlags & DBNODESTATS)

#define DBCOMPUTELOCSRIGHTABOVE 0x0200
#define DBcomputeLocsRightAbove (debugFlags & DBCOMPUTELOCSRIGHTABOVE)

#define DBCOMPUTELOCSNEG 0x0400
#define DBcomputeLocsNeg (debugFlags & DBCOMPUTELOCSNEG)

typedef struct node_t {
    char nodeID[40];/* ID of the node */
    int haspos;     /* 0 if no position, otherwise, has position */
    double xpos;    /* horizontal position */
    double ypos;    /* vertical position */
    int haspos2;    /* temp 2nd position: 0 if no position */
    double xpos2;   /* temp 2nd position: horizontal */
    double ypos2;   /* temp 2nd position: vertical */
    int sCnt;       /* count of edges where node is source */
    int seiFirst;   /* index of first edge where node is source */
    int seiLast;    /* index of last edge where node is source */
    int dCnt;       /* count of edges where node is dest */
    int deiFirst;   /* index of first edge where node is dest */
    int deiLast;    /* index of last edge where node is dest */
    int qniNext;    /* value of zero means not on queue, otherwise
                        index of next in queue or -1 */
} node_t;

#define MAX_NODES 100  /* max number of nodes */
node_t node[MAX_NODES]; /* nodes */
int cnode;              /* current number of nodes */
int qniHead;            /* head of queue of nodes */
int qniTail;            /* tail of queue of nodes */


typedef struct nodeDist_t {
    int ni1;        /* index of the first node */
    int ni2;        /* index of the second node */
    double dist;    /* distance between the nodes */
} nodeDist_t;

#define MAX_NODEDIST 5000   /* max number of node distances */
nodeDist_t nodeDist[MAX_NODEDIST];
int cnodeDist;

double minDist = 1e20;  /* minimum distance - start with a very large value */
#define FUZZ 0.05   /* error in measurement - 0.05 is 5 percent */

typedef struct edge_t {
    int valid;      /* if 0, then not valid. */
    int di;         /* index of distance */
    double dist;    /* distance between nodes */
#if 0 /* don't need, since index is the same */
    int ni1;        /* index of source node */
    int ni2;        /* index of dest node */
#endif
    int eiSniNext;  /* index of next source edge for source node */
    int eiSniPrev;  /* index of prev source edge for source node */
    int eiDniNext;  /* index of next dest edge for dest node */
    int eiDniPrev;  /* index of prev dest edge for dest node */
} edge_t;
edge_t edge[MAX_NODES][MAX_NODES];


double
dabs(double n)
{
    return (n < 0.0) ? -n : n;
} /* dabs */


/** nodeIndex - get index of a node, given ID
*
* call with:
*   nodeID - ID of a node
*
* returns:
*   -1 if node ID not found
*   Otherwise, index of node
*/
int nodeIndex(char *nodeID)
{
    int i;

    for (i = 0; i < cnode; i++) {
        if (strcmp(node[i].nodeID, nodeID) == 0) {
            return i;
        }
    }
    return -1;
} /* nodeIndex */

#if 0
/** nodeDistIndex - get the index of nodeDist
*
* call with:
*   nodeID1 - ID of first node
*   nodeID2 - ID of second node
*
* returns:
*   -1 if no distance cound be located
*   otherwise, the index of the nodeDist info
*/
int nodeDistIndex(char *nodeID1, char *nodeID2)
{
    int i;

    for (i = 0; i < cnodeDist; i++) {
        if (((strcmp(nodeDist[i].nodeID1, nodeID1) == 0) &&
                                    (strcmp(nodeDist[i].nodeID2, nodeID2) == 0)) ||
                ((strcmp(nodeDist[i].nodeID1, nodeID2) == 0) &&
                                    (strcmp(nodeDist[i].nodeID2, nodeID1) == 0))) {
            return i;
        }
    }
    return -1;
} /* nodeDistIndex */
#endif


/** approxEq - check if two locations are approximately equal
*
* note: uses global minDist, which is the minimal distance,
*           and FUZZ, which is the measurement error
*
* call with:
*   x1 - x position of first location
*   y1 - y position of first location
*   x2 - x position of second location
*   y2 - y position of second location
*
* returns:
*   1 if locations are approximately equal
*   0 otherwise
*/
int
approxEq(double x1, double y1, double x2, double y2)
{
    double dist;
    double normDist;
    double xdiff;
    double ydiff;

    xdiff = x1 - x2;
    ydiff = y1 - y2;
    dist = sqrt(xdiff*xdiff + ydiff*ydiff);
    normDist = dist/minDist;
    return (normDist < FUZZ) ? 1 : 0;
} /* approxEq */


/** computeNodeLocs - compute possible two locations for a node, given
*                       the locations of two other nodes and the distances
*                       from those other two nodes
* note: solving this is determining the intersection of two circles, where
*       the first circle has center which is the location of the first node
*       and radius which is the distance to the third node;
*       the second circle has center which is the location of the second node
*       and radius which is the distance to the third node.
*
*       the equations for the two circles are:
*           (x - x1)**2 + (y - y1)**2 = r1**2
*         and
*           (x - x2)**2 + (y - y2)**2 = r2**2
*
*       solving for equations for x and y results in the following two equations:
*           x = (x2 + x1)/2 + (x2 - x1)(r1**2 - r2**2)/2d**2 +/-
*                   (y2 - y1)sqrt(((r1 + r2)**2 - d**2)(d**2 - (r2 - r1)**2))/(2d**2)
*
*           y = (y2 + y1)/2 + (y2 - y1)(r1**2 - r2**2)/2d**2 +/-
*                   (x2 - x1)sqrt(((r1 + r2)**2 - d**2)(d**2 - (r2 - r1)**2))/(2d**2)
*
*           where d = sqrt((x2 - x1)**2 + (y2 - y1)**2)
*
*   The locations of the third node is the two computed values for x and y.
*   note that the circles can have no, 1 or 2 points of intersection. The
*   following describes the relationships between the distances and number
*   of intersections where
*       d12 is the distance between nodes 1 and 2 (which is d)
*       d13 is the distance between nodes 1 and 3 (which is r1)
*       d23 is the distance between nodes 2 and 3 (which is r2)
*   when d13 + d23 < d12, then there is no intersection
*   when d13 + d23 = d12, then there is one intersection
*   when d13 = d12 + d23, then there is one intersection
*   when d23 = d12 + d13, then there is one intersection
*   when d13 > d12 + d23, then there is no intersection
*   when d23 > d12 + d13, then there is no intersection
*   otherwise, there are two intersections, that is (d13 + d23 > d12) &
*                               (d13 < d12 + d23) & (d23 < d12 + d13)
* call with:
*   inode1 - index of first node
*   inode2 - index of second node
*   inode3 - index of third node (the one to determine the location)
*   d13 - distance from first node to third node, which is r1
*   d23 - distance from second node to third node, which is r2
*
* returns:
*   the number of locations calculated (which could be 1 or 2)
*/
int computeNodeLocs(int inode1, int inode2, int inode3,
                    double d13, double d23)
{
    double d12;         /* d */
    double xdiff;       /* x2 - x1 */
    double xsum;        /* x2 + x1 */
    double ydiff;       /* y2 - y1 */
    double ysum;        /* y2 + y1 */
    double rsqrdiff;    /* r1**2 - r2**2 */
    double rdiffsqr;    /* (r2 - r1)**2 */
    double rsumsqr;     /* (r2 + r1)**2 */
    double d12sqr;      /* d**2 */
#if 0
    double adjnu;       /* ((r2+r1)**2 - d**2)(d**2 - (r2-r1)**2) */
#endif
    double adj;         /* sqrt(((r2+r1)**2 - d**2)(d**2 - (r2-r1)**2))/(2*d**2) */
    int intercepts;     /* number of intercepts (0, 1, or 2) */
    double t;
    double tmp;
    double xpos;
    double ypos;
    double xpos2;
    double ypos2;
    double lxdelta;
    double hxdelta;
    double minxdelta;
    double lydelta;
    double hydelta;
    double minydelta;
    double mindist;
    double lxdelta2;
    double hxdelta2;
    double minxdelta2;
    double lydelta2;
    double hydelta2;
    double minydelta2;
    double mindist2;
    int i;

    if (DBcomputeLocs) {
        printf("computeNodeLocs called for "
                "node1=%s(index=%d), "
                "node2=%s(index=%d), "
                "node3=%s(index=%d)\n",
                node[inode1].nodeID, inode1,
                node[inode2].nodeID, inode2,
                node[inode3].nodeID, inode3);
    }

    /* compute some common terms */
    xdiff = node[inode2].xpos - node[inode1].xpos;
    ydiff = node[inode2].ypos - node[inode1].ypos;
    xsum = node[inode2].xpos + node[inode1].xpos;
    ysum = node[inode2].ypos + node[inode1].ypos;
    rsqrdiff = d13*d13 - d23*d23;
    rdiffsqr = d23 - d13;
    rdiffsqr *= rdiffsqr;
    rsumsqr = d23 + d13;
    rsumsqr *= rsumsqr;
    /* compute the distance between node1 and node2, and its square */
    d12 = sqrt(xdiff*xdiff + ydiff*ydiff);
    d12sqr = d12*d12;

#if 0
    adjnu = (rsumsqr - d12sqr)*(d12sqr - rdiffsqr);
#endif

#if 0
    /* check if there are no interceptions */
/*    if ((d13+d23 < d12) || (d13 > d12+d23) || (d23 > d12+d13)) { */

    if (adjnu < -0.000001) {
        printf("**bad values(no interception): "
                "node1=(%.5g,%.5g), node2=(%.5g,%.5g), "
                "d13=%.5g, d23=%.5g, d12=%.5g\n",
                node[inode1].xpos, node[inode1].ypos,
                node[inode2].xpos, node[inode2].ypos,
                d13, d23, d12);
        return 0;
    }
    /* check if one interception */
    intercepts = 1;     /* assume one intercept */
#if 0
    if (d13+d23 == d12) {
        /* node3 on the line between node1 and node2, and
            between node1 and node2 */
        printf("Node 3 is between node 1 and node 2\n");
    } else if (d13 == d12+d23) {
        /* node3 on the line between node1 and node2, and
            on the far side of node2 */
        printf("node 3 is on the far side of node 2\n");
    } else if (d23 == d12+d13) {
        /* node3 on the line between node1 and node2, and
            on the far side of node1 */
        printf("node 3 is on the far side of node 2\n");
    } else {
        /* there are two points of intersection */
        printf("there are two locations possible "
                "for node 3\n");
        intercepts = 2;
    }
#endif
    if (adjnu > 0.000001) {
        intercepts = 2;
    }
#endif

    /* check each case */
    t = (d13+d23)/d12;
    if (t < (1.0-FUZZ/2)) {
        /* r1 (d13) + r2 (d23) approx less than d (d12) */

        /* fix distances, so can compute location */
        /* change distances so node3 is between node1 and node2 */
        double nd13 = (d12+d13-d23)/2;
        double nd23 = d12 - nd13;

        if (DBcomputeLocsErr) {
            printf("**bad values(no interception): "
                  "node1=(%.5g,%.5g), node2=(%.5g,%.5g), "
                   "d13=%.5g, d23=%.5g, d12=%.5g; "
                   "changed to d13=%.5g, d23=%.5g\n",
                   node[inode1].xpos, node[inode1].ypos,
                   node[inode2].xpos, node[inode2].ypos,
                   d13, d23, d12, nd13, nd23);
        }
        /* change distances so node3 is between node1 and node2 */
        d13 = nd13;
        d23 = nd23;
        /* recompute intermediate terms */
        rsqrdiff = d13*d13 - d23*d23;
        rdiffsqr = d23 - d13;
        rdiffsqr *= rdiffsqr;
        rsumsqr = d23 + d13;
        rsumsqr *= rsumsqr;
        t = 1;
    }
    if (t < (1.0+FUZZ/2)) {
        /* r1 (d13) + r2 (d23) approx equal to d (d12) */
        /* node3 on the line between node1 and node2, and
            between node1 and node2 */
        if (DBcomputeLocsPos) {
            printf("Node 3 is between node 1 and node 2\n");
        }
        intercepts = 1;
    } else if (d13 > d23) {
        t = d13/(d12+d23);
        if (t > (1.0+FUZZ/2)) {
            /* r1 (d13) approx greater than r2 (d23) + d (d12) */

            /* fix distances, so can compute location */
            /* change distances so node3 is on the far side of node2 */
            double nd13 = (d12+d13+d23)/2;
            double nd23 = nd13 - d12;

            if (DBcomputeLocsErr) {
                printf("**bad values(no interception): "
                        "node1=(%.5g,%.5g), node2=(%.5g,%.5g), "
                        "d13=%.5g, d23=%.5g, d12=%.5g; "
                        "changed to d13=%.5g, d23=%.5g\n",
                        node[inode1].xpos, node[inode1].ypos,
                        node[inode2].xpos, node[inode2].ypos,
                        d13, d23, d12, nd13, nd23);
            }
            /* change distances so node3 is on the far side of node 2 */
            d13 = nd13;
            d23 = nd23;
            /* recompute intermediate terms */
            rsqrdiff = d13*d13 - d23*d23;
            rdiffsqr = d23 - d13;
            rdiffsqr *= rdiffsqr;
            rsumsqr = d23 + d13;
            rsumsqr *= rsumsqr;
            t = 1;
        }
        if (t < (1-FUZZ/2)) {
            if (DBcomputeLocsPos) {
                printf("there are two locations possible for "
                        "node3=%s(index=%d)\n",
                        node[inode3].nodeID, inode3);
            }
            intercepts = 2;
        } else {
            /* r1 (d13) approximately equal to r2 (d23) + d (d12) */
            /* node3 on the line containing node1 and node2, and
                on the far side of node2 */
            if (DBcomputeLocsPos) {
                printf("node3=%s(index=%d) is on the far side of "
                        "node2=%s(index=%d)\n",
                        node[inode3].nodeID, inode3,
                        node[inode2].nodeID, inode2);
            }
            intercepts = 1;
        }
    } else {
        t = d23/(d12+d13);
        if (t > (1+FUZZ/2)) {
            /* r2 (d23) approx greater than r1 (d13) + d (d12) */

            /* fix distances, so can compute location */
            /* change distances so node3 is on the far side of node1 */
            double nd23 = (d12+d13+d23)/2;
            double nd13 = nd23 - d12;

            if (DBcomputeLocsErr) {
                printf("**bad values(no interception): "
                        "node1(index=%d)=(%.5g,%.5g), "
                        "node2(index=%d)=(%.5g,%.5g), "
                        "d13=%.5g, d23=%.5g, d12=%.5g; "
                        "changed to d13=%.5g, d23=%.5g\n",
                        inode1, node[inode1].xpos, node[inode1].ypos,
                        inode2, node[inode2].xpos, node[inode2].ypos,
                        d13, d23, d12, nd13, nd23);
            }
            /* change distances so node3 is on the far side of node 2 */
            d13 = nd13;
            d23 = nd23;
            /* recompute intermediate terms */
            rsqrdiff = d13*d13 - d23*d23;
            rdiffsqr = d23 - d13;
            rdiffsqr *= rdiffsqr;
            rsumsqr = d23 + d13;
            rsumsqr *= rsumsqr;
            t = 1;
        }
        if (t < (1-FUZZ/2)) {
            if (DBcomputeLocsPos) {
                printf("there are two locations possible for "
                        "node3=%s(index=%d)\n",
                        node[inode3].nodeID, inode3);
            }
            intercepts = 2;
        } else {
            /* r2 (d23) approximately equal to r1 (d13) + d (d12) */
            /* node3 on the line containing node1 and node2, and
                on the far side of node1 */
            if (DBcomputeLocsPos) {
                printf("node3=%s(index=%d) is on the far side of "
                        "node1=%s(index=%d)\n",
                        node[inode3].nodeID, inode3,
                        node[inode1].nodeID, inode1);
            }
            intercepts = 1;
        }
    }
    if (intercepts == 1) {
        /* compute the location of node 3 */
        node[inode3].xpos = xsum/2 + xdiff*rsqrdiff/(2*d12sqr);
        if (fabs(node[inode3].xpos) <= FUZZ) {
            node[inode3].xpos = 0;
        }
        node[inode3].ypos = ysum/2 + ydiff*rsqrdiff/(2*d12sqr);
        if (fabs(node[inode3].ypos) <= FUZZ) {
            node[inode3].ypos = 0;
        }
        node[inode3].haspos = 1;

#if 0
        /* check if previously had two positions */
        if (node[inode3].haspos2) {
            printf("node 3 second position cleared\n");
            node[inode3].haspos2 = 0;
        }
#endif
    } else {
        /* node has two possible locations */

        /* compute adjustment term */
        adj = sqrt((rsumsqr - d12sqr)*(d12sqr - rdiffsqr))/(2*d12sqr);

#if 0
        /* compute the first location of node 3 */
        node[inode3].xpos = xsum/2 + xdiff*rsqrdiff/(2*d12sqr) + ydiff*adj;
        if (fabs(node[inode3].xpos <= FUZZ) {
            node[inode3].xpos = 0;
        }
        node[inode3].ypos = ysum/2 + ydiff*rsqrdiff/(2*d12sqr) - xdiff*adj;
        if (fabs(node[inode3].ypos <= FUZZ) {
            node[inode3].ypos = 0;
        }
        node[inode3].haspos = 1;

        /* compute the second location of node 3 */
        node[inode3].xpos2 = xsum/2 + xdiff*rsqrdiff/(2*d12sqr) - ydiff*adj;
        if (fabs(node[inode3].xpos2) <= FUZZ) {
            node[inode3].xpos2 = 0;
        }
        node[inode3].ypos2 = ysum/2 + ydiff*rsqrdiff/(2*d12sqr) + xdiff*adj;
        if (fabs(node[inode3].ypos2 <= FUZZ) {
            node[inode3].ypos2 = 0;
        }
        node[inode3].haspos2 = 1;
#endif
        /* compute the first location of node 3 */
        xpos = xsum/2 + xdiff*rsqrdiff/(2*d12sqr) + ydiff*adj;
        if (fabs(xpos) <= FUZZ) {
            xpos = 0;
        }
        ypos = ysum/2 + ydiff*rsqrdiff/(2*d12sqr) - xdiff*adj;
        if (fabs(ypos) <= FUZZ) {
            ypos = 0;
        }

        /* compute the second location of node 3 */
        xpos2 = xsum/2 + xdiff*rsqrdiff/(2*d12sqr) - ydiff*adj;
        if (fabs(xpos2) <= FUZZ) {
            xpos2 = 0;
        }
        ypos2 = ysum/2 + ydiff*rsqrdiff/(2*d12sqr) + xdiff*adj;
        if (fabs(ypos2) <= FUZZ) {
            ypos2 = 0;
        }

        if (DBcomputeLocsPos) {
            printf("computed locs for node %s(index=%d) are "
                    "(%g,%g) and (%g,%g)\n",
                    node[inode3].nodeID, inode3,
                    xpos, ypos, xpos2, ypos2);
        }

        /* check to see if a location already exists */
        if (node[inode3].haspos2) {
            /* node already has two positions */
            /* check if they are the same or different */
            /* Let ePos1 and ePos2 be the existing positions,
                and nPos1 and nPos2 be newly computed positions
                there are 8 cases, which are (with action):
                1) (ePos1 = nPos1) & (ePos2 = nPos2): do nothing
                2) (ePos1 = nPos2) & (ePos2 = nPos1): do nothing
                3) (ePos1 = nPos1) & (ePos2 != nPos2): discard pos2
                4) (ePos1 = nPos2) & (ePos2 != nPos1): discard pos2
                5) (ePos1 != nPos1) & (ePos2 != nPos2): do nothing
                6) (ePos1 != nPos2) & (ePos2 != nPos1): do nothing
                7) (ePos1 != nPos1) & (ePos2 = nPos2): set ePos1 to ePos2 &
                                                            discard pos2
                8) (ePos1 != nPos2) & (ePos2 = nPos1): set ePos1 to ePos2 &
                                                            discard pos2
            */
            if (approxEq(node[inode3].xpos, xpos, node[inode3].ypos, ypos) &&
                !approxEq(node[inode3].xpos2, xpos2, node[inode3].ypos2, ypos2)) {
                /* case #3 - discard pos2 */
                node[inode3].haspos2 = 0;
                intercepts = 1;
            } else
            if (approxEq(node[inode3].xpos, xpos2, node[inode3].ypos, ypos2) &&
                !approxEq(node[inode3].xpos2, xpos, node[inode3].ypos2, ypos)) {
                /* case #4 - discard pos2 */
                node[inode3].haspos2 = 0;
                intercepts = 1;
            } else
            if (!approxEq(node[inode3].xpos, xpos, node[inode3].ypos, ypos) &&
                approxEq(node[inode3].xpos2, xpos2, node[inode3].ypos2, ypos2)) {
                /* case #7 - discard pos2, and use computed pos2 */
                node[inode3].haspos2 = 0;
                node[inode3].xpos = xpos2;
                node[inode3].ypos = ypos2;
                intercepts = 1;
            } else
            if (!approxEq(node[inode3].xpos, xpos2, node[inode3].ypos, ypos2) &&
                approxEq(node[inode3].xpos2, xpos, node[inode3].ypos2, ypos)) {
                /* case #8 - discard pos2, and use computed pos1 */
                node[inode3].haspos2 = 0;
                node[inode3].xpos = xpos;
                node[inode3].ypos = ypos;
                intercepts = 1;
            }
            /* else - case 1, 2, 5, & 6 - do nothing */
        } else if (node[inode3].haspos) {
            /* node already has a single position */
            /* nothing to do */
        } else {
            /* node doesn't have a position yet */

            /* check if location matches an exising node */
            for (i = 0; i < cnode; i++) {
                if (i == inode3) {
                    continue;
                }
                if (node[i].haspos) {
/*                    if ((node[i].xpos == xpos) &&
                            (node[i].ypos == ypos)) {
*/
                    if (approxEq(node[i].xpos, node[i].ypos, xpos, ypos)) {
                        node[inode3].haspos = 1;
                        node[inode3].xpos = xpos2;
                        node[inode3].ypos = ypos2;
                        node[inode3].haspos2 = 1;
                        node[inode3].xpos2 = xpos;
                        node[inode3].ypos2 = ypos;
                        return 2;
                    }
/*                    if ((node[i].xpos == xpos2) &&
                            (node[i].ypos == ypos2)) {
*/
                    if (approxEq(node[i].xpos, node[i].ypos, xpos2, ypos2)) {
                        node[inode3].haspos = 1;
                        node[inode3].xpos = xpos;
                        node[inode3].ypos = ypos;
                        node[inode3].haspos2 = 1;
                        node[inode3].xpos2 = xpos2;
                        node[inode3].ypos2 = ypos2;
                        return 2;
                    }
                }
            }

            /* TODO: figure out what to do */

#if 0
            /* choose location to the right or above */
            /* TODO: check this - it doesn't look correct */
            if ((xdiff == 0) || (dabs(ydiff/xdiff) > 0.5)) {
                if (xpos < xpos2) {
                    /* swap positions */
                    node[inode3].xpos = xpos2;
                    node[inode3].xpos2 = xpos;
                    node[inode3].ypos = ypos2;
                    node[inode3].ypos2 = ypos;
                } else {
                    node[inode3].xpos = xpos;
                    node[inode3].xpos2 = xpos2;
                    node[inode3].ypos = ypos;
                    node[inode3].ypos2 = ypos2;
                }
            } else {
                if (ypos < ypos2) {
                    /* swap positions */
                    node[inode3].xpos = xpos2;
                    node[inode3].xpos2 = xpos;
                    node[inode3].ypos = ypos2;
                    node[inode3].ypos2 = ypos;
                } else {
                    node[inode3].xpos = xpos;
                    node[inode3].xpos2 = xpos2;
                    node[inode3].ypos = ypos;
                    node[inode3].ypos2 = ypos2;
                }
            }
#endif
#if 0
            /* choose location to the right or above */
            if (DBcomputeLocsRightAbove) {
                printf("Checking position for node3=%s(index=%d), "
                        "xdiff=%g, ydiff=%g\n",
                        node[inode3].nodeID, inode3,
                        xdiff, ydiff);
            }
            if ((xdiff == 0) || (dabs(ydiff/xdiff) > 0.5)) {
                if (xpos < xpos2) {
                    /* swap positions */
                    tmp = xpos;
                    xpos = xpos2;
                    xpos2 = tmp;
                    tmp = ypos;
                    ypos = ypos2;
                    ypos2 = tmp;
                }
            } else {
                if (ypos < ypos2) {
                    /* swap positions */
                    tmp = xpos;
                    xpos = xpos2;
                    xpos2 = tmp;
                    tmp = ypos;
                    ypos = ypos2;
                    ypos2 = tmp;
                }
            }
#else
            /* swap locations if have negative one */
            if ((xpos <= FUZZ) || (ypos <= FUZZ)) {
                /* swap positions */
                if (DBcomputeLocsNeg) {
                    printf("Swapping positions for node3=%s(index=%d), "
                            "have negative for pos1\n",
                            node[inode3].nodeID, inode3);
                }
                tmp = xpos;
                xpos = xpos2;
                xpos2 = tmp;
                tmp = ypos;
                ypos = ypos2;
                ypos2 = tmp;
            }
#endif

            /* select the position that is closest to a grid */
            lxdelta = xpos - floor(xpos);
            hxdelta = floor(xpos+0.5) - xpos;
            minxdelta = (lxdelta < hxdelta) ? lxdelta : hxdelta;
            lydelta = ypos - floor(ypos);
            hydelta = floor(ypos+0.5) - ypos;
            minydelta = (lydelta < hydelta) ? lydelta : hydelta;
            mindist = sqrt(minxdelta*minxdelta + minydelta*minydelta);
            if (mindist <= FUZZ) {
                mindist = 0;
            }

            lxdelta2 = xpos2 - floor(xpos2);
            hxdelta2 = floor(xpos2+0.5) - xpos2;
            minxdelta2 = (lxdelta2 < hxdelta2) ? lxdelta2 : hxdelta2;
            lydelta2 = ypos2 - floor(ypos2);
            hydelta2 = floor(ypos2+0.5) - ypos2;
            minydelta2 = (lydelta2 < hydelta2) ? lydelta2 : hydelta2;
            mindist2 = sqrt(minxdelta2*minxdelta2 + minydelta2*minydelta2);
            if (mindist2 <= FUZZ) {
                mindist2 = 0;
            }

            if (DBcomputeLocsGridDelta) {
                printf("Using distance to grid for node3=%s(index=%d), "
                        "d1=%g, d2=%g\n",
                        node[inode3].nodeID, inode3,
                        mindist, mindist2);
            }
            if (mindist <= mindist2) {
                node[inode3].xpos = xpos;
                node[inode3].xpos2 = xpos2;
                node[inode3].ypos = ypos;
                node[inode3].ypos2 = ypos2;
            } else {
                node[inode3].xpos = xpos2;
                node[inode3].xpos2 = xpos;
                node[inode3].ypos = ypos2;
                node[inode3].ypos2 = ypos;
            }
            node[inode3].haspos = 1;
            node[inode3].haspos2 = 1;
        }
    }

    return intercepts;
} /* computeNodeLocs */

/** add2queue - add node to queue
*
* call with:
*   ni - node index
*/
void
add2queue(int ni)
{
    if (node[ni].qniNext != 0) {
        if (DBadd2Queue) {
            printf("**Trying to add node=%s(index=%d) to queue, "
                    "but already in queue\n",
                    node[ni].nodeID, ni);
        }
        return;
    }
    if (DBadd2Queue) {
        printf("Adding node=%s(index=%d) to queue\n",
                node[ni].nodeID, ni);
    }

    /* add to queue */
    if (qniHead == -1) {
        /* queue empty */
        qniHead = ni;
    } else {
        /* add to tail */
        node[qniTail].qniNext = ni;
    }
    qniTail = ni;
    node[ni].qniNext = -1;
} /* add2queue */


/* computeLocsR - resursive version of compute locations
*
* call with:
*   ni - index of a node with a known location
*/
void
computeLocsR(int ni)
{
    int j;
    int k;
    int intercepts;


    if (DBcomputLocsR) {
        printf("computeLocsR called for node with index=%d\n", ni);
    }

    /* for each distance with this node as source, get destination node */
    for (j = node[ni].seiFirst; j != -1; j = edge[ni][j].eiSniNext) {
        /* j is index of destination node of distance */
/*        if (node[j].haspos & !node[j].haspos2) { */
        if (node[j].haspos) {
            /* location of node is known, so skip */
            continue;
        }
        /* find other nodes with a known location with a distance to node j */
        for (k = node[j].deiFirst; k != -1; k = edge[k][j].eiDniNext) {
            if (k == ni) {
                /* skip base node */
                continue;
            }
            if (!node[k].haspos) {
                /* skip nodes with no known location */
                continue;
            }
            if (node[j].haspos) {
                /* skip nodes that already have a location */
                continue;
            }
            /* compute location of node[j] */
            intercepts = computeNodeLocs(ni, k, j,
                                    edge[ni][j].dist, edge[k][j].dist);
            /* add node to queue */
            if (intercepts ==1) {
                add2queue(j);
            }
       }
    }

} /* computeLocsR */

#if 0
/* computeLocsR - resursive version of compute locations
*
* call with:
*   ni1 - index of first node with known location
*   ni2 - index of second node with known location
*
*/
void
computeLocsR(int ni1, int ni2)
{
    int j;

    for (j = node[ni1].seiFirst; j != -1; j = edge[ni1][j].eiSniNext) {
        if (node[j].haspos & !node[j].haspos2) {
            continue;
        }
        if (edge[ni2][j].valid) {
            /* compute location of node[j] */
            intercepts = computeNodeLocs(ni1, ni2, j,
                                    edge[ni1][j].dist, edge[ni2][j].dist);
       }
    }

} /* computeLocsR */
#endif


/** computeLocs - compute the location of each node
* Assumptions (for now):
*   1) there are no locations specified for the nodes
*   2) the distances are correct (that is, the same results will be determined
*       no matter which nodes are used)
*/
void computeLocs(void)
{
    int i;
    int j;
    int k;
    int cnt;


    /* init queue of nodes to be empty */
    qniHead = -1;
    qniTail = -1;
    /* add all nodes with a location to the node queue */
    for (i = 0, cnt = 0; i < cnode; i++) {
        if (node[i].haspos) {
            add2queue(i);
            cnt++;
        }
    }
    /* check less than two nodes have locations */
    if (cnt <= 1) {
        if (cnt == 0) {
            /* no node had a location, so assign the first node to (0,0) */
            node[0].haspos = 1;
            node[0].xpos = 0;
            node[0].ypos = 0;
            add2queue(0);
        }

        /* no second node had a location, so choose a second node
            that is closest to first node */
        i = qniHead;
        for (j = node[i].seiFirst, k = j; j != -1; j = edge[i][j].eiSniNext) {
            if (edge[i][j].dist < edge[i][k].dist) {
                k = j;
            }
        }
        /* assume the second node is directly above the first node */
        node[k].haspos = 1;
        node[k].xpos = 0;
        node[k].ypos = edge[i][k].dist;
        add2queue(k);
    }

#if 0
    /* walk through nodes computing locations */
    /* TODO: change to breadth first traversal of graph */

    /* start with all nodes that can be seen from node[i] and node[k] */
    for (j = node[i].seiFirst; j != -1; j = edge[i][j].eiSniNext) {
        if (node[j].haspos & !node[j].haspos2) {
            continue;
        }
        if (edge[k][j].valid) {
            /* compute location of node[j] */
            intercepts = computeNodeLocs(i, k, j,
                                    edge[i][j].dist, edge[k][j].dist);
       }
    }
#else
    do {
        /* empty the queue */
        while (qniHead != -1) {
            /* remove item from queue */
            k = qniHead;
            if (qniHead == qniTail) {
                /* empty queue */
                qniHead = -1;
                qniTail = -1;
            } else {
                qniHead = node[k].qniNext;
            }
            node[k].qniNext = 0;    /* show not in queue */
            computeLocsR(k);
        }

        /* find first node with two locs and add to queue */
        for (i = 0, cnt = 0; i < cnode; i++) {
            if (node[i].haspos && node[i].haspos2) {
                node[i].haspos2 = 0;
                add2queue(i);
                cnt++;
                break;
            }
        }
    } while(cnt != 0);
#endif

} /* computeLocs */

void
addDist(int inode, int jnode, double dist)
{
    int k;

    nodeDist[cnodeDist].ni1 = inode;
    nodeDist[cnodeDist].ni2 = jnode;
    nodeDist[cnodeDist].dist = dist;

    /* add to source list */
    if (node[inode].sCnt == 0) {
        /* list is empty */
        node[inode].seiFirst = jnode;
        node[inode].seiLast = jnode;
        edge[inode][jnode].eiSniNext = -1;
        edge[inode][jnode].eiSniPrev = -1;
    } else if (node[inode].seiFirst > jnode) {
        /* insert at the head of the list */
        edge[inode][jnode].eiSniNext = node[inode].seiFirst;
        edge[inode][jnode].eiSniPrev = -1;
        edge[inode][node[inode].seiFirst].eiSniPrev = jnode;
        node[inode].seiFirst = jnode;
    } else if (node[inode].seiLast < jnode) {
        /* insert at the end of the list */
        edge[inode][jnode].eiSniNext = -1;
        edge[inode][jnode].eiSniPrev = node[inode].seiLast;
        edge[inode][node[inode].seiLast].eiSniNext = jnode;
        node[inode].seiLast = jnode;
    } else {
        /* insert in the list, find place */
        for (k = node[inode].seiFirst; k < jnode;
                                k = edge[inode][k].eiSniNext) {
        }
        /* insert before */
        edge[inode][jnode].eiSniNext = k;
        edge[inode][jnode].eiSniPrev = edge[inode][k].eiSniPrev;
        edge[inode][edge[inode][k].eiSniPrev].eiSniNext = jnode;
        edge[inode][k].eiSniPrev = jnode;
    }
    node[inode].sCnt++;
    /* add to dest list */
    if (node[jnode].dCnt == 0) {
        /* list is empty */
        node[jnode].deiFirst = inode;
        node[jnode].deiLast = inode;
        edge[inode][jnode].eiDniNext = -1;
        edge[inode][jnode].eiDniPrev = -1;
    } else if (node[jnode].deiFirst > inode) {
        /* insert at the head of the list */
        edge[inode][jnode].eiDniNext = node[jnode].deiFirst;
        edge[inode][jnode].eiDniPrev = -1;
        edge[node[jnode].deiFirst][jnode].eiDniPrev = inode;
        node[jnode].deiFirst = inode;
    } else if (node[jnode].deiLast < inode) {
        /* insert at the end of the list */
        edge[inode][jnode].eiDniNext = -1;
        edge[inode][jnode].eiDniPrev = node[jnode].deiLast;
        edge[node[jnode].deiLast][jnode].eiDniNext = inode;
        node[jnode].deiLast = inode;
    } else {
        /* insert in the list, find place */
        for (k = node[jnode].deiFirst; k < inode;
                                k = edge[k][jnode].eiDniNext) {
        }
        /* insert before */
        edge[inode][jnode].eiDniNext = k;
        edge[inode][jnode].eiDniPrev = edge[k][jnode].eiDniPrev;
        edge[edge[k][jnode].eiDniPrev][jnode].eiDniNext = inode;
        edge[k][jnode].eiDniPrev = inode;
    }
    node[jnode].dCnt++;

    /* finish initializing edge */
    edge[inode][jnode].valid = 1;
    edge[inode][jnode].di = cnodeDist;
    edge[inode][jnode].dist = nodeDist[cnodeDist].dist;

    cnodeDist++;
} /* addDist */


/** check_revdist - for each distance, check that the reverse exists,
*                   and is equal
*/
void
check_revdist()
{
    int k;
    int i;
    int j;
    double t;

    for (k = 0; k < cnodeDist; k++) {
        i = nodeDist[k].ni1;
        j = nodeDist[k].ni2;
        if (edge[i][j].valid && edge[j][i].valid) {
            if (edge[i][j].dist == edge[j][i].dist) {
                /* both distances present and are equal */
                continue;
            }
            t = (edge[i][j].dist + edge[j][i].dist)/2;
            if (DBaddRevDist) {
                printf("**distance from %s(index=%d) to %s(index=%d) is %.5g, "
                        "but reverse is %.5g. Distance averaged to %.5g\n",
                        node[i].nodeID, i, node[j].nodeID, j,
                        edge[i][j].dist, edge[j][i].dist, t);
            }
            edge[i][j].dist = t;
            edge[j][i].dist = t;
        } else if (!edge[i][j].valid) {
            if (DBaddRevDist) {
                printf("**missing edge from %s(index=%d) to %s(index=%d) "
                        "for distance with index %d\n",
                        node[i].nodeID, i, node[j].nodeID, j, k);
            }
        } else if (!edge[j][i].valid) {
            if (DBaddRevDist) {
                printf("**missing reverse distance from %s(index=%d) to %s(index=%d), "
                        "added\n",
                        node[j].nodeID, j,
                        node[i].nodeID, i);
            }
            addDist(j, i, edge[i][j].dist);
        }
    }
} /* check_revdist */


void
print_nodes(void)
{
    int i;
    int k;

    if (DBfileStats) {
        printf("Number of nodes: %d\n", cnode);
        printf("Number of edges: %d\n", cnodeDist);
    }

    if (DBnodeStats) {
        for (i = 0; i < cnode; i++) {
            printf("  %4d: Node: %s: src=%d, dst=%d",
                    i, node[i].nodeID, node[i].sCnt, node[i].dCnt);
            if (node[i].haspos) {
                printf(", p1=(%.5g, %.5g)",
                        node[i].xpos, node[i].ypos);
            }
            if (node[i].haspos2) {
                printf(", p2=(%.5g, %.5g)",
                        node[i].xpos2, node[i].ypos2);
            }
            printf("\n");
            for (k = node[i].seiFirst; k != -1; k = edge[i][k].eiSniNext) {
                printf("        edge[%d][%d]: v=%s, d[%d]=%.5g\n",
                        i, k, edge[i][k].valid ? "yes" : "no",
                        edge[i][k].di, edge[i][k].dist);
            }
            for (k = node[i].deiFirst; k != -1; k = edge[k][i].eiDniNext) {
                printf("        edge[%d][%d]: v=%s, d[%d]=%.5g\n",
                        k, i, edge[k][i].valid ? "yes" : "no",
                        edge[k][i].di, edge[k][i].dist);
            }
        }
        printf("\n");
    }
    for (i = 0; i < cnode; i++) {
        printf("%s, %.5g, %.5g\n",
                node[i].nodeID,
                node[i].xpos, node[i].ypos);
    }
    printf("\n");

    if (DBnodeStats) {
        for (i = 0; i < cnodeDist; i++) {
            printf("  %4d: Dist: %s(index=%d) to %s(index=%d) is %.5g\n",
                    i, node[nodeDist[i].ni1].nodeID, nodeDist[i].ni1,
                    node[nodeDist[i].ni2].nodeID, nodeDist[i].ni2,
                    nodeDist[i].dist);
        }
    }
} /* print_nodes */


int main(int argc, char **argv)
{
    FILE *nFh;
    int errnum;
    char line[80];
    char *pline;
    enum {
        readingNodes = 1,
        readingDistances = 2,
        doneReading = 3
    } inputState = readingNodes;
    int linenum = 0;
    size_t len;
    int cnt;
    int inode;
    int jnode;
    char nodeID1[40];
    char nodeID2[40];
    double dist;

    /* check for debug flag */
    if ((argc > 1) && (strcmp(argv[1], "-d") == 0)) {
        debugFlags = 0xffffffff;
        argc--;
        argv++;
    }

#if 0 /* old usage - specify filename on program command line */
    if (argc != 2) {
        printf("Usage is map <filename>\n");
        exit(1);
    }

    nFh = fopen(argv[1], "r");
    if (!nFh) {
        errnum = errno;
        printf("**Error openning file \"%s\": %s\n",
            argv[1], strerror(errnum));
        exit(1);
    }
#else /* new way - input file is stdin */
    if (argc > 1) {
        if ((argc != 3) || (strcmp(argv[1], "-f") != 0)) {
            printf("Usage is map -f <filename>\n");
            exit(1);
        }
        nFh = fopen(argv[2], "r");
        if (!nFh) {
            errnum = errno;
            printf("**Error openning file \"%s\": %s\n",
                argv[2], strerror(errnum));
            exit(1);
        }
    } else {
        nFh = stdin;
    }
#endif


    while((pline = fgets(line, sizeof(line), nFh)) != 0) {
        len = strlen(pline);
        if (len < 1) {
            break;
        }
        linenum++;
        line[--len] = 0; /* remove the trailing \n */
        if (len == 0) {
            if ((readingNodes != inputState) || (0 != cnode)) {
                inputState++;
                if (inputState == doneReading) {
                    break;
                }
            }
            continue;
        }
        if (line[0] == '#') {
            /* a comment - skip */
            continue;
        }
        switch (inputState) {
        case readingNodes:
            /* parse a line containing a node definition */
            cnt = sscanf(line, "%39s %lg %lg",
                        &node[cnode].nodeID[0],
                        &node[cnode].xpos,
                        &node[cnode].ypos);
            if (cnt != 3) {
                printf("**On line %d, couldn't read nodeID, xpos, and ypos\n",
                    linenum);
                continue;
            }
            if ((inode = nodeIndex(node[cnode].nodeID)) != -1) {
                printf("**On line %d, nodeID %s already used\n",
                        linenum, node[cnode].nodeID);
                break;
            }
            if ((node[cnode].xpos != -1) && (node[cnode].ypos != -1)) {
                node[cnode].haspos = 1;
            }
            node[cnode].seiFirst = -1;
            node[cnode].seiLast = -1;
            node[cnode].deiFirst = -1;
            node[cnode].deiLast = -1;
            cnode++;
            break;
        case readingDistances:
            /* parse a line containing a distance specification */
            cnt = sscanf(line, "%39s %39s %lg",
                        &nodeID1[0],
                        &nodeID2[0],
                        &dist);
            if (cnt != 3) {
                printf("**On line %d, couldn't read nodeID1, "
                        "nodeID2, and dist\n",
                        linenum);
                continue;
            }
            if (dist <= 0) {
                printf("**On line %d, distance (%.5g) less than or equal zero\n",
                    linenum, nodeDist[cnodeDist].dist);
                continue;
            }
            if (dist < minDist) {
                minDist = dist;
            }
            inode = nodeIndex(nodeID1);
            jnode = nodeIndex(nodeID2);
            if (inode == -1) {
                printf("**On line %d, node %s is not known\n",
                    linenum, nodeID1);
                continue;
            }
            if (jnode == -1) {
                printf("**On line %d, node %s is not known\n",
                    linenum, nodeID2);
                continue;
            }
            if (edge[inode][jnode].valid) {
                printf("**On line %d, Already have have a "
                        "distance for node %s to node %s\n",
                        linenum, nodeID1, nodeID2);
                continue;
            }
            addDist(inode, jnode, dist);
            break;
        case doneReading:
            break;
        }
    }

    if (DBfileStats) {
        printf("File contained %d line%s\n",
                linenum, (linenum == 1) ? "" : "s");
        printf("File contained %d node%s\n",
                cnode, (cnode == 1) ? "" : "s");
        printf("File contained %d distance%s\n",
                cnodeDist, (cnodeDist == 1) ? "" : "s");
    }
/*    print_nodes(); */

    if (cnode < 3) {
        printf("**Err: must specify at least 3 nodes\n");
        exit(1);
    }

    check_revdist();
    computeLocs();
    print_nodes();

    exit(0);
} /* main */


