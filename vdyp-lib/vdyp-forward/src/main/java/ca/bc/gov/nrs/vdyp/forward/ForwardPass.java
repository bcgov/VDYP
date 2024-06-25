package ca.bc.gov.nrs.vdyp.forward;

//
//     VDYPPASS    IN/OUT    I*4(10)   Major Control Functions
//       (1)       IN          Perform Initiation activities? (0=No, 1=Yes)
//       (2)       IN          Open the stand data files (0=No, 1=Yes)
//       (3)       IN          Process stands (0=No, 1=Yes)
//       (4)       IN          Allow multiple polygons (0=No, 1=Yes)
//                              (Subset of stand processing. May limit to 1 stand)
//       (5)       IN          CLOSE data files.
//       (10)      OUT         Indicator variable that in the case of single stand processing
//                             with VDYPPASS(4) set, behaves as follows:
//                             -100  due to EOF, nothing to read
//                              other -ve value, incl -99.  Could not process the stand.
//                             0   Stand was processed and written
//                             +ve value.  Serious error. Set to IER.

public enum ForwardPass {
	PASS_1, PASS_2, PASS_3, PASS_4, PASS_5, PASS_6
}
