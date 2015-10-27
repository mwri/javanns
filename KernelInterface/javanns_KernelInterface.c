/*
 * Java-to-SNNS kernel interface
 * by Igor Fischer
 *
 * based on Tcl or Perl Extension with SNNS-Functions by Fred Rapp,
 * which is itself based on Jens Wieland's batchman
 *
 */


#include "javanns_KernelInterface.h"
#pragma warning( disable : 4244 )


#define BUFLEN  255

#include <time.h>
/* Taken from Fred's batch.c */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "glob_typ.h"     /*  SNNS-Kernel constants and data type definitions  */
#include "cc_mac.h"       /*  SNNS-Kernel macros for cascade correlation       */
#include "kr_ui.h"        /*  SNNS-Kernel User-Interface Function Prototypes   */
#include "art_typ.h"      /*  Must include for art_ui.h (I.F.)                 */
#include "art_ui.h"       /*  SNNS-Kernel ART Interface Function Prototypes    */
#include "prun_f.h"       /*  SNNS-Kernel constants for pruning function calls */

//#include "batch.h"
//#include "globals.h"
//#include "except.h"

#define NO_OF_CASCADE_PARAMS 23  /* UI_NO_OF_CASCADE_PARAMS as defined in xgui/ui.h */

time_t start;
char patternFileNames[NO_OF_PAT_SETS][BUFLEN];
int currentPatternSet = -1;
int i, k, ival;
int initnum, learnnum, ffLearnnum, updatenum, resultnum, cyclenum = 0;
int lastErrorCode = KRERR_NO_ERROR;
double val;
float init[NO_OF_INIT_PARAMS];
float learn[NO_OF_LEARN_PARAMS + NO_OF_CASCADE_PARAMS];
float ffLearn[NO_OF_LEARN_PARAMS]; /* I.F.: do we need this array at all? */
float update[NO_OF_UPDATE_PARAMS];
float *result, fval;

/* Pruning variables */
float max_error_incr, accepted_error, min_error_to_stop, init_matrix_value; 
bool recreatef, input_pruningf, hidden_pruningf;
int first_train_cyc, retrain_cyc;
char *pruneTmpFile;

int 
    spIsize[MAX_NO_OF_VAR_DIM],/* parameters for subpattern definition */
    spIstep[MAX_NO_OF_VAR_DIM],
    spOsize[MAX_NO_OF_VAR_DIM],
    spOstep[MAX_NO_OF_VAR_DIM];

int
    init_net_flag = FALSE,     /* init_param_array init'ed or not */
    init_learn_flag = FALSE,   /* learn_param_array init'ed or not */
    init_ffLearn_flag = FALSE, /* ffLearn_param_array init'ed or not */
    init_update_flag = FALSE,  /* update_param_array init'ed or not */
    init_subPat_flag = FALSE;  /* subPattern arrays init'ed or not */


/***************************************************************************************
 *                                   Error handling                                    *
 ***************************************************************************************/

bool isOK(JNIEnv *jEnv, int err_code) {
  lastErrorCode = err_code;
  if ( (err_code <= 0) && (err_code != KRERR_NO_ERROR) ) {
    jclass newExcCls;

printf("%s\n\a", krui_error(err_code));
    newExcCls = (*jEnv)->FindClass(jEnv, "javanns/KernelInterface$KernelException");
    if (newExcCls == 0) { /* Unable to find the new exception class, give up. */
      printf("Can't throw exception, giving up!\a\n");
      return FALSE;
    }
    (*jEnv)->ThrowNew(jEnv, newExcCls, krui_error(err_code));
    return FALSE;
  }
  return TRUE;
}
 
void error(JNIEnv *jEnv, const char *s) {
  jclass newExcCls;
  
  newExcCls = (*jEnv)->FindClass(jEnv, "javanns/KernelInterface$KernelException");
  if (newExcCls == 0) { /* Unable to find the new exception class, give up. */
    printf("Can't throw exception, giving up!\a\n");
    return;
  }
  (*jEnv)->ThrowNew(jEnv, newExcCls, s);
}



/***************************************************************************************
 *                                Some auxiliary functions                             *
 ***************************************************************************************/
 

/*
 * trainFFNet is called from various pruning methods, as well as from
 *   the interface method Java_javanns_KernelInterface_trainFFNet__I
 */
float trainFFNet(JNIEnv *jEnv, jobject jObj, int cycles ) {
  float *return_values;
  int NoOfOutParams;
  int cycle = 0;

  while(
    (cycle < cycles) &&
    ((cycle && (return_values [0] > min_error_to_stop)) || !cycle) &&
    isOK(jEnv, krui_learnAllPatternsFF(learn, learnnum, &return_values, &NoOfOutParams) )
  ) cycle++;

  return (return_values[0]);
}



/***************************************************************************************
 *                             Actual interface functions                              *
 ***************************************************************************************/
 
/*
 * Class:     javanns_KernelInterface
 * Method:    getNoOfUnits
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNoOfUnits(JNIEnv *jEnv, jobject jObj) {
  return krui_getNoOfUnits();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNoOfSpecialUnits
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNoOfSpecialUnits(JNIEnv *jEnv, jobject jObj) {
  error(jEnv, "Non-existent kernel function!");
  return 0;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getFirstUnit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getFirstUnit(JNIEnv *jEnv, jobject jObj) {
  return krui_getFirstUnit();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNextUnit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNextUnit(JNIEnv *jEnv, jobject jObj) {
  return krui_getNextUnit();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setCurrentUnit
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setCurrentUnit(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  isOK(jEnv, krui_setCurrentUnit(unitNo) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getCurrentUnit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getCurrentUnit(JNIEnv *jEnv, jobject jObj) {
  return krui_getCurrentUnit();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getUnitName(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  char *p = krui_getUnitName(unitNo);
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitName
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitName(JNIEnv *jEnv, jobject jObj,
                                 jint unitNo,
                                 jstring jUnitName) {
  const char *unitName = (*jEnv)->GetStringUTFChars(jEnv, jUnitName, 0);
  isOK(
    jEnv,
    krui_setUnitName( unitNo, (char *)unitName)
  );
  (*jEnv)->ReleaseStringUTFChars(jEnv, jUnitName, unitName);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    searchUnitName
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_searchUnitName(JNIEnv *jEnv, jobject jObj, jstring jUnitName) {
  const char *unitName = (*jEnv)->GetStringUTFChars(jEnv, jUnitName, 0);
  int i = krui_searchUnitName( (char *)unitName);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jUnitName, unitName);
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    searchNextUnitName
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_searchNextUnitName(JNIEnv *jEnv, jobject jObj){
  int i = krui_searchNextUnitName();
  isOK(jEnv, i);
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitOutFuncName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getUnitOutFuncName(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  char *p = krui_getUnitOutFuncName(unitNo);
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitActFuncName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getUnitActFuncName(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  char *p = krui_getUnitActFuncName(unitNo);
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitOutFunc
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitOutFunc(JNIEnv *jEnv, jobject jObj,
                                    jint unitNo,
                                    jstring jName) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jName, 0);
  int i = krui_setUnitOutFunc(unitNo, (char *)name);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jName, name);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitActFunc
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitActFunc(JNIEnv *jEnv, jobject jObj,
                                    jint unitNo,
                                    jstring jName) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jName, 0);
  int i = krui_setUnitActFunc(unitNo, (char *)name);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jName, name);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitFTypeName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getUnitFTypeName(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  char *p = krui_getUnitFTypeName(unitNo);
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitActivation
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL
Java_javanns_KernelInterface_getUnitActivation(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  return krui_getUnitActivation(unitNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitActivation
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitActivation(JNIEnv *jEnv, jobject jObj,
                                       jint unitNo,
                                       jdouble act) {
  isOK(jEnv, krui_setUnitActivation(unitNo, act) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitInitialActivation
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL
Java_javanns_KernelInterface_getUnitInitialActivation(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  return krui_getUnitInitialActivation(unitNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitInitialActivation
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitInitialActivation(JNIEnv *jEnv, jobject jObj,
                                       jint unitNo,
                                       jdouble act) {
  krui_setUnitInitialActivation(unitNo, act);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitOutput
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL
Java_javanns_KernelInterface_getUnitOutput(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  return krui_getUnitOutput(unitNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitOutput
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitOutput(JNIEnv *jEnv, jobject jObj,
                                       jint unitNo,
                                       jdouble out) {
  isOK(jEnv, krui_setUnitOutput(unitNo, out) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitBias
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL
Java_javanns_KernelInterface_getUnitBias(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  return krui_getUnitBias(unitNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitBias
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitBias(JNIEnv *jEnv, jobject jObj,
                                       jint unitNo,
                                       jdouble bias) {
  krui_setUnitBias(unitNo, bias);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitSubnetNo
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getUnitSubnetNo(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  return krui_getUnitSubnetNo(unitNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitSubnetNo
 * Signature: (II)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitSubnetNo(JNIEnv *jEnv, jobject jObj,
                                     jint unitNo, jint subnetNo) {
  krui_setUnitSubnetNo(unitNo, subnetNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitLayerNo
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getUnitLayerNo(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  return krui_getUnitLayerNo(unitNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitLayerNo
 * Signature: (II)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitLayerNo(JNIEnv *jEnv, jobject jObj,
                                    jint unitNo,
                                    jint layerNo) {
  krui_setUnitLayerNo(unitNo, layerNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitPosition
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_getUnitPosition(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  struct PosType pos;

  krui_getUnitPosition(unitNo, &pos);

  fid = (*jEnv)->GetFieldID(jEnv, cls, "posX", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, pos.x);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "posY", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, pos.y);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "posZ", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, pos.z);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitPosition
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitPosition(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  struct PosType pos;

  fid = (*jEnv)->GetFieldID(jEnv, cls, "posX", "I");
  if(fid) pos.x = (*jEnv)->GetIntField(jEnv, jObj, fid);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "posY", "I");
  if(fid) pos.y = (*jEnv)->GetIntField(jEnv, jObj, fid);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "posZ", "I");
  if(fid) pos.z = (*jEnv)->GetIntField(jEnv, jObj, fid);

  krui_setUnitPosition(unitNo, &pos);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitNoAtPosition
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getUnitNoAtPosition(JNIEnv *jEnv, jobject jObj, jint subnetNo) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  struct PosType pos;

  fid = (*jEnv)->GetFieldID(jEnv, cls, "posX", "I");
  if(fid) pos.x = (*jEnv)->GetIntField(jEnv, jObj, fid);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "posY", "I");
  if(fid) pos.y = (*jEnv)->GetIntField(jEnv, jObj, fid);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "posZ", "I");
  if(fid) pos.z = (*jEnv)->GetIntField(jEnv, jObj, fid);

  return krui_getUnitNoAtPosition(&pos, subnetNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitNoNearPosition
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getUnitNoNearPosition(JNIEnv *jEnv, jobject jObj,
                                           jint subnetNo,
                                           jint range,
                                           jint gridWidth) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  struct PosType pos;

  fid = (*jEnv)->GetFieldID(jEnv, cls, "posX", "I");
  if(fid) pos.x = (*jEnv)->GetIntField(jEnv, jObj, fid);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "posY", "I");
  if(fid) pos.y = (*jEnv)->GetIntField(jEnv, jObj, fid);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "posZ", "I");
  if(fid) pos.z = (*jEnv)->GetIntField(jEnv, jObj, fid);

  return krui_getUnitNoNearPosition(&pos, subnetNo, range, gridWidth);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitTType
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getUnitTType(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  return krui_getUnitTType(unitNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitTType
 * Signature: (II)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitTType(JNIEnv *jEnv, jobject jObj,
                                  jint unitNo,
                                  jint unitTType) {
  isOK(jEnv, krui_setUnitTType(unitNo, unitTType) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    freezeUnit
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_freezeUnit(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  isOK(jEnv, krui_freezeUnit(unitNo) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    unfreezeUnit
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_unfreezeUnit(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  isOK(jEnv, krui_unfreezeUnit(unitNo) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    isUnitFrozen
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_isUnitFrozen(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  return krui_isUnitFrozen(unitNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitInputType
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getUnitInputType(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  return krui_getUnitInputType(unitNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitValueA
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL
Java_javanns_KernelInterface_getUnitValueA(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  return krui_getUnitValueA(unitNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitValueA
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitValueA(JNIEnv *jEnv, jobject jObj,
                                   jint unitNo,
                                   jdouble unitValueA) {
  krui_setUnitValueA(unitNo, unitValueA);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    createDefaultUnit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_createDefaultUnit(JNIEnv *jEnv, jobject jObj) {
  int i = krui_createDefaultUnit();
  isOK(jEnv, i);
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    createUnit
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DD)I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_createUnit(JNIEnv *jEnv, jobject jObj,
                                jstring jName,
                                jstring jOutFn,
                                jstring jActFn,
                                jdouble iAct,
                                jdouble bias) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jName, 0);
  const char *outFn = (*jEnv)->GetStringUTFChars(jEnv, jOutFn, 0);
  const char *actFn = (*jEnv)->GetStringUTFChars(jEnv, jActFn, 0);
  int i = krui_createUnit((char *)name, (char *)outFn, (char *)actFn, iAct, bias);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jName, name);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jOutFn, outFn);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jActFn, actFn);
  isOK(jEnv, i);
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    createFTypeUnit
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_createFTypeUnit(JNIEnv *jEnv, jobject jObj, jstring jFtName) {
  const char *ftName = (*jEnv)->GetStringUTFChars(jEnv, jFtName, 0);
  int i = krui_createFTypeUnit( (char *)ftName );
  (*jEnv)->ReleaseStringUTFChars(jEnv, jFtName, ftName);
  isOK(jEnv, i);
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitFType
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitFType(JNIEnv *jEnv, jobject jObj,
                                  jint unitNo,
                                  jstring jFtName) {
  const char *ftName = (*jEnv)->GetStringUTFChars(jEnv, jFtName, 0);
  int i = krui_setUnitFType( unitNo, (char *)ftName );
  (*jEnv)->ReleaseStringUTFChars(jEnv, jFtName, ftName);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    copyUnit
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_copyUnit(JNIEnv *jEnv, jobject jObj,
                              jint unitNo,
                              jint copyMode) {
  int i = krui_copyUnit(unitNo, copyMode);
  isOK(jEnv, i);
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    deleteUnitList
 * Signature: ([I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_deleteUnitList(JNIEnv *jEnv, jobject jObj, jintArray jUnitNos) {
  int i;
  jsize len = (*jEnv)->GetArrayLength(jEnv, jUnitNos);
  jint *units = (*jEnv)->GetIntArrayElements(jEnv, jUnitNos, 0);
  int *unit_array = (int *)malloc(len*sizeof(int));

  if(unit_array) {
    for(i=0; i<len; i++) unit_array[i] = units[i];
    i = krui_deleteUnitList(len, unit_array);
    free(unit_array);
  }
  else {
    error(jEnv, "Out of memory: malloc failed in deleteUnitList");
    i=0;
  }
  (*jEnv)->ReleaseIntArrayElements(jEnv, jUnitNos, units, 0);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    createSiteTableEntry
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_createSiteTableEntry(JNIEnv *jEnv, jobject jObj,
                                          jstring jSiteName,
                                          jstring jSiteFn) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jSiteName, 0);
  const char *fn = (*jEnv)->GetStringUTFChars(jEnv, jSiteFn, 0);
  int i = krui_createSiteTableEntry( (char *)name, (char *)fn);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jSiteFn, fn);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jSiteName, name);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    changeSiteTableEntry
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_changeSiteTableEntry(JNIEnv *jEnv, jobject jObj,
                                          jstring jOldSiteName,
                                          jstring jNewSiteName,
                                          jstring jNewSiteFn) {
  const char *oldName = (*jEnv)->GetStringUTFChars(jEnv, jOldSiteName, 0);
  const char *newName = (*jEnv)->GetStringUTFChars(jEnv, jNewSiteName, 0);
  const char *fn = (*jEnv)->GetStringUTFChars(jEnv, jNewSiteFn, 0);
  int i = krui_changeSiteTableEntry( (char *)oldName, (char *)newName, (char *)fn);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jNewSiteFn, fn);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jNewSiteName, newName);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jOldSiteName, oldName);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    deleteSiteTableEntry
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_deleteSiteTableEntry(JNIEnv *jEnv, jobject jObj, jstring jSiteName) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jSiteName, 0);
  int i = krui_deleteSiteTableEntry( (char *)name );
  (*jEnv)->ReleaseStringUTFChars(jEnv, jSiteName, name);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getFirstSiteTableEntry
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_getFirstSiteTableEntry(JNIEnv *jEnv, jobject jObj) {
  char name[128];
  char fn[128];
  char *p_name = name;
  char *p_fn = fn;
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;

  bool b = krui_getFirstSiteTableEntry( &p_name, &p_fn );

  fid = (*jEnv)->GetFieldID(jEnv, cls, "siteName", "Ljava/lang/String;");
  if(fid) (*jEnv)->SetObjectField(jEnv, jObj, fid, (*jEnv)->NewStringUTF(jEnv, name ) );
  fid = (*jEnv)->GetFieldID(jEnv, cls, "siteFunction", "Ljava/lang/String;");
  if(fid) (*jEnv)->SetObjectField(jEnv, jObj, fid, (*jEnv)->NewStringUTF(jEnv, fn ) );

  return b;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNextSiteTableEntry
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_getNextSiteTableEntry(JNIEnv *jEnv, jobject jObj) {
  char name[128];
  char fn[128];
  char *p_name = name;
  char *p_fn = fn;
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;

  bool b = krui_getNextSiteTableEntry( &p_name, &p_fn );

  fid = (*jEnv)->GetFieldID(jEnv, cls, "siteName", "Ljava/lang/String;");
  if(fid) (*jEnv)->SetObjectField(jEnv, jObj, fid, (*jEnv)->NewStringUTF(jEnv, name ) );
  fid = (*jEnv)->GetFieldID(jEnv, cls, "siteFunction", "Ljava/lang/String;");
  if(fid) (*jEnv)->SetObjectField(jEnv, jObj, fid, (*jEnv)->NewStringUTF(jEnv, fn ) );

  return b;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getSiteTableFuncName
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getSiteTableFuncName(JNIEnv *jEnv, jobject jObj, jstring jSiteName) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jSiteName, 0);
  char *fn = krui_getSiteTableFuncName( (char *)name);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jSiteName, name);
  if(fn) return (*jEnv)->NewStringUTF(jEnv, fn );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setFirstSite
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_setFirstSite(JNIEnv *jEnv, jobject jObj) {
  return krui_setFirstSite();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setNextSite
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_setNextSite(JNIEnv *jEnv, jobject jObj) {
  return krui_setNextSite();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setSite
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_setSite(JNIEnv *jEnv, jobject jObj, jstring jSiteName) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jSiteName, 0);
  bool b = krui_setSite( (char *)name);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jSiteName, name);
  return b;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getSiteValue
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL
Java_javanns_KernelInterface_getSiteValue(JNIEnv *jEnv, jobject jObj) {
  return krui_getSiteValue();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getSiteFuncName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getSiteFuncName(JNIEnv *jEnv, jobject jObj) {
  char *p = krui_getSiteFuncName();
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getSiteName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getSiteName(JNIEnv *jEnv, jobject jObj) {
  char *p = krui_getSiteName();
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setSiteName
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setSiteName(JNIEnv *jEnv, jobject jObj, jstring jSiteName) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jSiteName, 0);
  int i = krui_setSiteName( (char *)name);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jSiteName, name);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    addSite
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_addSite(JNIEnv *jEnv, jobject jObj, jstring jSiteName) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jSiteName, 0);
  int i = krui_addSite( (char *)name);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jSiteName, name);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    deleteSite
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_deleteSite(JNIEnv *jEnv, jobject jObj) {
  return krui_deleteSite();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getFirstPredUnit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getFirstPredUnit(JNIEnv *jEnv, jobject jObj) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  FlintType weight;
  int i =  krui_getFirstPredUnit( &weight );
  isOK(jEnv,i);

  fid = (*jEnv)->GetFieldID(jEnv, cls, "link_weight", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, weight);

  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNextPredUnit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNextPredUnit(JNIEnv *jEnv, jobject jObj) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  FlintType weight;
  int i =  krui_getNextPredUnit( &weight );
  isOK(jEnv,i);

  fid = (*jEnv)->GetFieldID(jEnv, cls, "link_weight", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, weight);

  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getCurrentPredUnit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getCurrentPredUnit(JNIEnv *jEnv, jobject jObj) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  FlintType weight;
  int i =  krui_getCurrentPredUnit( &weight );
  isOK(jEnv,i);

  fid = (*jEnv)->GetFieldID(jEnv, cls, "link_weight", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, weight);

  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getFirstSuccUnit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getFirstSuccUnit(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  FlintType weight;
  int i =  krui_getFirstSuccUnit(unitNo, &weight);
  isOK(jEnv,i);

  fid = (*jEnv)->GetFieldID(jEnv, cls, "link_weight", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, weight);

  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNextSuccUnit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNextSuccUnit(JNIEnv *jEnv, jobject jObj) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  FlintType weight;
  int i =  krui_getNextSuccUnit( &weight );
  isOK(jEnv,i);

  fid = (*jEnv)->GetFieldID(jEnv, cls, "link_weight", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, weight);

  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    isConnected
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_isConnected(JNIEnv *jEnv, jobject jObj, jint sourceUnitNo) {
  return krui_isConnected(sourceUnitNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    areConnected
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_areConnected(JNIEnv *jEnv, jobject jObj,
                                  jint sourceUnitNo,
                                  jint targetUnitNo) {
  return krui_areConnected(sourceUnitNo, targetUnitNo);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    areConnectedWeight
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_areConnectedWeight(JNIEnv *jEnv, jobject jObj,
                                        jint sourceUnitNo,
                                        jint targetUnitNo) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  FlintType weight;
  bool b = krui_areConnectedWeight(sourceUnitNo, targetUnitNo, &weight );

  fid = (*jEnv)->GetFieldID(jEnv, cls, "link_weight", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, weight);

  return b;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getLinkWeight
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL
Java_javanns_KernelInterface_getLinkWeight(JNIEnv *jEnv, jobject jObj) {
  return krui_getLinkWeight();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setLinkWeight
 * Signature: (D)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setLinkWeight(JNIEnv *jEnv, jobject jObj, jdouble weight) {
  krui_setLinkWeight(weight);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    createLink
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_createLink(JNIEnv *jEnv, jobject jObj,
                                jint sourceUnitNo,
                                jdouble weight) {
  krui_createLink(sourceUnitNo, weight);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    deleteLink
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_deleteLink(JNIEnv *jEnv, jobject jObj) {
  isOK(jEnv, krui_deleteLink() );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    deleteAllInputLinks
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_deleteAllInputLinks(JNIEnv *jEnv, jobject jObj) {
  isOK(jEnv, krui_deleteAllInputLinks() );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    deleteAllOutputLinks
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_deleteAllOutputLinks(JNIEnv *jEnv, jobject jObj) {
  isOK(jEnv, krui_deleteAllOutputLinks() );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    jogWeights
 * Signature: (DD)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_jogWeights(JNIEnv *jEnv, jobject jObj,
                                jdouble minus,
                                jdouble plus) {
  krui_jogWeights(minus, plus);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    jogCorrWeights
 * Signature: (DDD)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_jogCorrWeights(JNIEnv *jEnv, jobject jObj,
                                    jdouble minus,
                                    jdouble plus,
                                    jdouble mincorr) {
  krui_jogCorrWeights(minus, plus, mincorr);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setFirstFTypeEntry
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_setFirstFTypeEntry(JNIEnv *jEnv, jobject jObj) {
  return krui_setFirstFTypeEntry();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setNextFTypeEntry
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_setNextFTypeEntry(JNIEnv *jEnv, jobject jObj) {
  return krui_setNextFTypeEntry();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setFTypeEntry
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_setFTypeEntry(JNIEnv *jEnv, jobject jObj, jstring jName) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jName, 0);
  bool b = krui_setFTypeEntry( (char *) name);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jName, name);
  return b;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getFTypeName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getFTypeName(JNIEnv *jEnv, jobject jObj) {
  char *p = krui_getFTypeName();
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setFTypeName
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setFTypeName(JNIEnv *jEnv, jobject jObj, jstring jName) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jName, 0);
  int i = krui_setFTypeName( (char *)name);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jName, name);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getFTypeActFuncName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getFTypeActFuncName(JNIEnv *jEnv, jobject jObj) {
  char *p = krui_getFTypeActFuncName();
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setFTypeActFunc
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setFTypeActFunc(JNIEnv *jEnv, jobject jObj, jstring jName) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jName, 0);
  int i = krui_setFTypeActFunc( (char *)name);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jName, name);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getFTypeOutFuncName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getFTypeOutFuncName(JNIEnv *jEnv, jobject jObj) {
  char *p = krui_getFTypeOutFuncName();
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setFTypeOutFunc
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setFTypeOutFunc(JNIEnv *jEnv, jobject jObj, jstring jName) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jName, 0);
  int i = krui_setFTypeOutFunc( (char *)name);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jName, name);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setFirstFTypeSite
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_setFirstFTypeSite(JNIEnv *jEnv, jobject jObj) {
  return krui_setFirstFTypeSite();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setNextFTypeSite
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_setNextFTypeSite(JNIEnv *jEnv, jobject jObj) {
  return krui_setNextFTypeSite();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getFTypeSiteName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getFTypeSiteName(JNIEnv *jEnv, jobject jObj) {
  char *p = krui_getFTypeSiteName();
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setFTypeSiteName
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setFTypeSiteName(JNIEnv *jEnv, jobject jObj, jstring jName) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jName, 0);
  int i = krui_setFTypeSiteName( (char *)name);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jName, name);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    createFTypeEntry
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_createFTypeEntry(JNIEnv *jEnv, jobject jObj,
                                      jstring jFType,
                                      jstring jActFn,
                                      jstring jOutFn,
                                      jobjectArray jSiteNames) {
/*
  int i;
  const char *ftype = (*jEnv)->GetStringUTFChars(jEnv, jFType, 0);
  const char *actfn = (*jEnv)->GetStringUTFChars(jEnv, jActFn, 0);
  const char *outfn = (*jEnv)->GetStringUTFChars(jEnv, jOutFn, 0);
  jsize len = (*jEnv)->GetArrayLength(jEnv, jSiteNames);
  char **siteNames = (char **) malloc ( len * sizeof(char *) );
  if(!siteNames) {
    error(jEnv, "Malloc failed in createFTypeEntry");
    return;
  }
  jstring *p_jSiteNames = (*jEnv)->GetObjectArrayElements(jEnv, jSiteNames, 0);
  for(i=0; i<len; i++) {
    siteNames[i] = (*jEnv)->GetStringUTFChars(jEnv, p_jSiteNames[i], 0);
  }

  int i = krui_createFTypeEntry(ftype, actfn, outfn, len, siteNames);
  
  for(i=0; i<len; i++) {
    (*jEnv)->ReleaseStringUTFChars(jEnv, p_jSiteNames[i], siteNames[i]);
  }
  (*jEnv)->ReleaseObjectArrayElements(jEnv, jSiteNames, p_jSiteNames, 0);
  free(siteNames);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jOutFn, outfn);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jActFn, actfn);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jFType, ftype);
  isOK(jEnv, i);
*/
}

/*
 * Class:     javanns_KernelInterface
 * Method:    deleteFTypeEntry
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_deleteFTypeEntry(JNIEnv *jEnv, jobject jObj, jstring jFType) {
  const char *ftype = (*jEnv)->GetStringUTFChars(jEnv, jFType, 0);
  krui_deleteFTypeEntry( (char *)ftype);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jFType, ftype);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNoOfFunctions
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNoOfFunctions(JNIEnv *jEnv, jobject jObj) {
  return krui_getNoOfFunctions();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getFuncInfo
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_getFuncInfo(JNIEnv *jEnv, jobject jObj, jint funcNo) {
  char name[128];
  char *p_name = name;
  int i;
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;

  krui_getFuncInfo( funcNo, &p_name, &i );

  fid = (*jEnv)->GetFieldID(jEnv, cls, "functionName", "Ljava/lang/String;");
  if(fid) (*jEnv)->SetObjectField(jEnv, jObj, fid, (*jEnv)->NewStringUTF(jEnv, name ) );
  fid = (*jEnv)->GetFieldID(jEnv, cls, "functionType", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    isFunction
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_isFunction(JNIEnv *jEnv, jobject jObj,
                                jstring jFnName,
                                jint fnType) {
  const char *fnName = (*jEnv)->GetStringUTFChars(jEnv, jFnName, 0);
  bool b = krui_isFunction( (char *)fnName, fnType);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jFnName, fnName);
  return b;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getFuncParamInfo
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_getFuncParamInfo(JNIEnv *jEnv, jobject jObj,
                                      jstring jFnName,
                                      jint fnType) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  int i, j;
  const char *fnName = (*jEnv)->GetStringUTFChars(jEnv, jFnName, 0);

  bool b = krui_getFuncParamInfo( (char *)fnName, fnType, &i, &j);

  (*jEnv)->ReleaseStringUTFChars(jEnv, jFnName, fnName);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "function_inputs", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "function_outputs", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, j);
  return b;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setInitFunc
 * Signature: (Ljava/lang/String;[D)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setInitFunc(JNIEnv *jEnv, jobject jObj,
                                 jstring jFunction_name,
                                 jdoubleArray jParams) {
  jsize i;
  const char *function_name = (*jEnv)->GetStringUTFChars(jEnv, jFunction_name, 0);
  jsize len = (*jEnv)->GetArrayLength(jEnv, jParams);
  jdouble *params = (*jEnv)->GetDoubleArrayElements(jEnv, jParams, 0);

  isOK(jEnv, krui_setInitialisationFunc( (char *)function_name) );
  (*jEnv)->ReleaseStringUTFChars(jEnv, jFunction_name, function_name);

  /* save values in corresponding global array */
  for(i=0; i<len; i++) init[i] = params[i];
  (*jEnv)->ReleaseDoubleArrayElements(jEnv, jParams, params, 0);

  for(; i<5; i++) init[i] = 0;
  initnum = 5;
  
  /* to enable to warn the user, set an init flag: */
  init_net_flag = TRUE;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getInitFunc
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getInitFunc(JNIEnv *jEnv, jobject jObj) {
  char *p = krui_getInitialisationFunc();
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    initNet
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_initNet(JNIEnv *jEnv, jobject jObj) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;

  /* initialize init parameter array:  */
  if (! init_net_flag) {
    for(i=0; i<NO_OF_INIT_PARAMS; i++) init[i] = 0.0;
    init_net_flag = TRUE;
  }

  isOK(jEnv, krui_initializeNet(init, initnum) );
  cyclenum = 0;
  fid = (*jEnv)->GetFieldID(jEnv, cls, "cycles", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, cyclenum);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    updateSingleUnit
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_updateSingleUnit(JNIEnv *jEnv, jobject jObj, jint unitNo) {
  isOK(jEnv, krui_updateSingleUnit(unitNo) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUpdateFunc
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getUpdateFunc(JNIEnv *jEnv, jobject jObj) {
  char *p = krui_getUpdateFunc();
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUpdateFunc
 * Signature: (Ljava/lang/String;[D)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUpdateFunc(JNIEnv *jEnv, jobject jObj,
                                   jstring jFunction_name,
                                   jdoubleArray jParams) {
  jsize i;
  const char *function_name = (*jEnv)->GetStringUTFChars(jEnv, jFunction_name, 0);
  jsize len = (*jEnv)->GetArrayLength(jEnv, jParams);
  jdouble *params = (*jEnv)->GetDoubleArrayElements(jEnv, jParams, 0);

  isOK(jEnv, krui_setUpdateFunc( (char *)function_name ) );
  (*jEnv)->ReleaseStringUTFChars(jEnv, jFunction_name, function_name);

  /* save values in corresponding global array */
  for(i=0; i<len; i++) update[i] = params[i];
  (*jEnv)->ReleaseDoubleArrayElements(jEnv, jParams, params, 0);

  for(; i<5; i++) update[i] = 0;
  updatenum = 5;
  init_update_flag = TRUE;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    updateNet
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_updateNet(JNIEnv *jEnv, jobject jObj) {

  /* initialize update parameter array:  */
  if (! init_update_flag) {
    for(i=0; i<NO_OF_UPDATE_PARAMS; i++) update[i] = 0.0;
    init_update_flag = TRUE;
  }

  isOK(jEnv, krui_updateNet(update, updatenum) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setLearnFunc
 * Signature: (Ljava/lang/String;[D)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setLearnFunc(JNIEnv *jEnv, jobject jObj,
                                  jstring jFunction_name,
                                  jdoubleArray jParams) {
  jsize i;
  const char *function_name = (*jEnv)->GetStringUTFChars(jEnv, jFunction_name, 0);
  jsize len = (*jEnv)->GetArrayLength(jEnv, jParams);
  jdouble *params = (*jEnv)->GetDoubleArrayElements(jEnv, jParams, 0);

  isOK(jEnv, krui_setLearnFunc( (char *)function_name) );
  (*jEnv)->ReleaseStringUTFChars(jEnv, jFunction_name, function_name);

  /* save values in corresponding global array */
  for(i=0; i<len; i++) learn[i] = params[i];
  (*jEnv)->ReleaseDoubleArrayElements(jEnv, jParams, params, 0);

  for(; i<5; i++) learn[i] = 0;
  learnnum = 5;
  init_learn_flag = TRUE;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setFFLearnFunc
 * Signature: (Ljava/lang/String;[D)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setFFLearnFunc(JNIEnv *jEnv, jobject jObj,
                                    jstring jFunction_name,
                                    jdoubleArray jParams) {
/* I.F.: do we need this method at all? */
  jsize i;
  const char *function_name = (*jEnv)->GetStringUTFChars(jEnv, jFunction_name, 0);
  jsize len = (*jEnv)->GetArrayLength(jEnv, jParams);
  jdouble *params = (*jEnv)->GetDoubleArrayElements(jEnv, jParams, 0);

  isOK(jEnv, krui_setFFLearnFunc( (char *)function_name) );
  (*jEnv)->ReleaseStringUTFChars(jEnv, jFunction_name, function_name);

  /* save values in corresponding global array */
  for(i=0; i<len; i++) ffLearn[i] = params[i];
  (*jEnv)->ReleaseDoubleArrayElements(jEnv, jParams, params, 0);

  for(; i<5; i++) ffLearn[i] = 0;
  ffLearnnum = 5;
  init_ffLearn_flag = TRUE;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setPruningFunc
 * Signature: (Ljava/lang/String;Ljava/lang/String;DDZIIDDZZ)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setPruningFunc(JNIEnv *jEnv, jobject jObj,
                                    jstring jPrune_func,
                                    jstring jLearn_func,
                                    jdouble jPmax_error_incr,
                                    jdouble jPaccepted_error,
                                    jboolean jPrecreatef,
                                    jint jPfirst_train_cyc,
                                    jint jPretrain_cyc,
                                    jdouble jPmin_error_to_stop,
                                    jdouble jPinit_matrix_value,
                                    jboolean jPinput_pruningf,
                                    jboolean jPhidden_pruningf) {

  const char *prune_func = (*jEnv)->GetStringUTFChars(jEnv, jPrune_func, 0);
  const char *learn_func = (*jEnv)->GetStringUTFChars(jEnv, jLearn_func, 0);
  bool ok = TRUE;
  if(!isOK(jEnv, krui_setPrunFunc( (char *)prune_func) )) ok = FALSE;
  if(!isOK(jEnv, krui_setFFLearnFunc( (char *)learn_func) )) ok = FALSE;
  (*jEnv)->ReleaseStringUTFChars(jEnv, jPrune_func, prune_func);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jLearn_func, learn_func);
  if(!ok) return;
  
  /* copy parameters to global values */
  max_error_incr = jPmax_error_incr;
  accepted_error = jPaccepted_error;
  recreatef = jPrecreatef;
  first_train_cyc = jPfirst_train_cyc;
  retrain_cyc = jPretrain_cyc;
  min_error_to_stop = jPmin_error_to_stop;
  init_matrix_value = jPinit_matrix_value;
  input_pruningf = jPinput_pruningf;
  hidden_pruningf = jPhidden_pruningf;
  
  pr_obs_setInitParameter(init_matrix_value);
  pr_setInputPruning(input_pruningf);
  pr_setHiddenPruning(hidden_pruningf);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getLearnFunc
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getLearnFunc(JNIEnv *jEnv, jobject jObj) {
  char *p = krui_getLearnFunc();
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getFFLearnFunc
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getFFLearnFunc(JNIEnv *jEnv, jobject jObj) {
  char *p = krui_getFFLearnFunc();
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getPrunFunc
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getPrunFunc(JNIEnv *jEnv, jobject jObj) {
  char *p = krui_getPrunFunc();
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    trainNet
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_trainNet__I(JNIEnv *jEnv, jobject jObj, jint trainsteps) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  int i;

  /* initialize learn parameter array if not already done: */
  if (! init_learn_flag) {
    learn[0] = 0.2f;
    for(i=1; i<NO_OF_LEARN_PARAMS; i++) learn[i] = 0.0;
    init_learn_flag = TRUE;
  }

  /* Train net once or several times */
  for(i=0;
      i<trainsteps &&
        isOK(jEnv, krui_learnAllPatterns(learn, NO_OF_LEARN_PARAMS, &result, &resultnum) );
      i++);
  cyclenum += i;

  // pass results to Java:
  fid = (*jEnv)->GetFieldID(jEnv, cls, "cycles", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, cyclenum);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "sse", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, resultnum ? result[0] : -1);

  i = krui_getTotalNoOfSubPatterns();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "subpatterns", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "mse", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, resultnum && i ? result[0]/i : -1);

  i = krui_getNoOfOutputUnits();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "output_units", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "ssepu", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, resultnum && i ? result[0]/i : -1);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    trainFFNet
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_trainFFNet__I(JNIEnv *jEnv, jobject jObj, jint trainsteps) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;

  trainFFNet(jEnv, jObj, trainsteps);

  // pass results to Java:
  fid = (*jEnv)->GetFieldID(jEnv, cls, "cycles", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, cyclenum);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "sse", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]);

  i = krui_getTotalNoOfSubPatterns();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "subpatterns", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "mse", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]/i);

  i = krui_getNoOfOutputUnits();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "output_units", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "ssepu", "D");
  if(fid) {
    /* set built-in variable SSEPU only if output units are present: */
    if(i != 0) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]/i);
    else (*jEnv)->SetDoubleField(jEnv, jObj, fid, -1);
  }
}

/*
 * Class:     javanns_KernelInterface
 * Method:    trainNet
 * Signature: (II)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_trainNet__II(JNIEnv *jEnv, jobject jObj, jint pat, jint trainsteps) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  int i;

  /* initialize learn parameter array if not already done: */
  if (! init_learn_flag) {
    learn[0] = 0.2f;
    for(i=1; i<NO_OF_LEARN_PARAMS; i++) learn[i] = 0.0;
    init_learn_flag = TRUE;
  }

  /* Train net once or several times */
  for(i=0;
      i<trainsteps &&
        isOK(jEnv, krui_learnSinglePattern(pat, learn, NO_OF_LEARN_PARAMS, &result, &resultnum) );
      i++);
  cyclenum += i;

  // pass results to Java:
  fid = (*jEnv)->GetFieldID(jEnv, cls, "cycles", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, cyclenum);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "sse", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]);

  i = krui_getTotalNoOfSubPatterns();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "subpatterns", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "mse", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]/i);

  i = krui_getNoOfOutputUnits();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "output_units", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "ssepu", "D");
  if(fid) {
    /* set built-in variable SSEPU only if output units are present: */
    if(i != 0) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]/i);
    else (*jEnv)->SetDoubleField(jEnv, jObj, fid, -1);
  }
}

/*
 * Class:     javanns_KernelInterface
 * Method:    trainFFNet
 * Signature: (II)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_trainFFNet__II(JNIEnv *jEnv, jobject jObj,
                                    jint pat,
                                    jint trainsteps) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  float *return_values;
  int NoOfOutParams;
  int cycle = 0;

  while(
    (cycle < trainsteps) &&
    ((cycle && (return_values [0] > min_error_to_stop)) || !cycle) &&
    isOK(jEnv, krui_learnSinglePatternFF(pat, learn, learnnum, &return_values, &NoOfOutParams) )
  ) cycle++;

  // pass results to Java:
  fid = (*jEnv)->GetFieldID(jEnv, cls, "cycles", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, cyclenum);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "sse", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]);

  i = krui_getTotalNoOfSubPatterns();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "subpatterns", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "mse", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]/i);

  i = krui_getNoOfOutputUnits();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "output_units", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "ssepu", "D");
  if(fid) {
    /* set built-in variable SSEPU only if output units are present: */
    if(i != 0) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]/i);
    else (*jEnv)->SetDoubleField(jEnv, jObj, fid, -1);
  }
}


/*
 * Class:     KernelInterface
 * Method:    trainNetFixedTime
 * Signature: (II)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_trainNetFixedTime__II(JNIEnv *jEnv, jobject jObj, jint trainsteps, jint d) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  int i;
  jdouble *body;
  jdoubleArray arr;
  jsize array_len;


  /* get java member variable */
  fid = (*jEnv)->GetFieldID(jEnv, cls, "sseArr", "[D");
  arr = (*jEnv)->GetObjectField(jEnv, jObj, fid);
  body = (*jEnv)->GetDoubleArrayElements(jEnv, arr, 0);
  array_len = (*jEnv)->GetArrayLength(jEnv, arr);

  /*initialize learn parameter array if not already done:*/
  if (! init_learn_flag) {
    learn[0] = 0.2f;
    for(i=1; i<NO_OF_LEARN_PARAMS; i++) learn[i] = 0.0;
    init_learn_flag = TRUE;
  }

  /*Train net once or several times*/
  start = time(NULL);
  body[0] = -1;
  for(i=0; i < array_len  &&
      ( i<trainsteps &&
        isOK(jEnv, krui_learnAllPatterns(learn, NO_OF_LEARN_PARAMS, &result, &resultnum) ) );
      i++) {
             body[i] = result[0];
	     if( time(NULL) > start +  d ) { ++i; break; } /* break if timeout */
	   }
  cyclenum += i;

  /* pass results to Java: */
  (*jEnv)->ReleaseDoubleArrayElements(jEnv, arr, body, 0);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "cycles", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, cyclenum);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "steps_done", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);

  i = krui_getTotalNoOfSubPatterns();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "subpatterns", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "mse", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, resultnum && i ? result[0]/i : -1);

  i = krui_getNoOfOutputUnits();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "output_units", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "ssepu", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, resultnum && i ? result[0]/i : -1);
}

/*
 * Class:     KernelInterface
 * Method:    trainNetFixedTime
 * Signature: (III)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_trainNetFixedTime__III(JNIEnv *jEnv, jobject jObj, jint pat, jint trainsteps,jint d) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  int i;
  jdouble *body;
  jdoubleArray arr;
  jsize array_len;

  /* get java member variable */
  fid = (*jEnv)->GetFieldID(jEnv, cls, "sseArr", "[D");
  arr = (*jEnv)->GetObjectField(jEnv, jObj, fid);
  body = (*jEnv)->GetDoubleArrayElements(jEnv, arr, 0);
  array_len = (*jEnv)->GetArrayLength(jEnv, arr);

  /* initialize learn parameter array if not already done :*/
  if (! init_learn_flag) {
    learn[0] = 0.2f;
    for(i=1; i<NO_OF_LEARN_PARAMS; i++) learn[i] = 0.0;
    init_learn_flag = TRUE;
  }

  /* Train net once or several times */
  start = time(NULL);
  body[0] = -1;
  for(i=0; i < array_len &&
     ( i<trainsteps &&
        isOK(jEnv, krui_learnSinglePattern(pat, learn, NO_OF_LEARN_PARAMS, &result, &resultnum) ) );
      i++) {
             body[i] = result[0];
             if( time(NULL) > start +  d ) { ++i; break; } /* break if timeout */
	   }
  cyclenum += i;

  /* pass results to Java: */
  (*jEnv)->ReleaseDoubleArrayElements(jEnv, arr, body, 0);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "cycles", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, cyclenum);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "steps_done", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);

  i = krui_getTotalNoOfSubPatterns();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "subpatterns", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "mse", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]/i);

  i = krui_getNoOfOutputUnits();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "output_units", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "ssepu", "D");
  if(fid) {
    /* set built-in variable SSEPU only if output units are present: */
    if(i != 0) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]/i);
    else (*jEnv)->SetDoubleField(jEnv, jObj, fid, -1);
  }
}


/*
 * Class:     javanns_KernelInterface
 * Method:    setPatternNo
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setPatternNo(JNIEnv *jEnv, jobject jObj, jint patNo) {
  isOK(jEnv, krui_setPatternNo(patNo) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getPatternNo
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getPatternNo(JNIEnv *jEnv, jobject jObj) {
  int i = krui_getPatternNo();
  isOK(jEnv, i);
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    deletePattern
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_deletePattern(JNIEnv *jEnv, jobject jObj) {
  isOK(jEnv, krui_deletePattern() );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    modifyPattern
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_modifyPattern(JNIEnv *jEnv, jobject jObj) {
  isOK(jEnv, krui_modifyPattern() );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setRemapFunc
 * Signature: (Ljava/lang/String;[D)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setRemapFunc(JNIEnv *jEnv, jobject jObj,
                                  jstring jName,
                                  jdoubleArray jParams) {
  jsize i;
  int ok;
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jName, 0);
  jsize len = (*jEnv)->GetArrayLength(jEnv, jParams);
  jdouble *params = (*jEnv)->GetDoubleArrayElements(jEnv, jParams, 0);
  float *fparams = (float *) malloc (len * sizeof(float));
  if(!fparams) {
    (*jEnv)->ReleaseDoubleArrayElements(jEnv, jParams, params, 0);
    (*jEnv)->ReleaseStringUTFChars(jEnv, jName, name);
    error(jEnv, "Out of memory: malloc failed in setRemapFunc");
    return;
  }
  for(i=0; i<len; i++) fparams[i] = params[i];

  ok = krui_setRemapFunc( (char *)name, fparams);

  free(fparams);
  (*jEnv)->ReleaseDoubleArrayElements(jEnv, jParams, params, 0);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jName, name);

  isOK(jEnv, ok);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    showPattern
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_showPattern(JNIEnv *jEnv, jobject jObj, jint mode) {
  isOK(jEnv, krui_showPattern(mode) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    newPattern
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_newPattern(JNIEnv *jEnv, jobject jObj) {
  isOK(jEnv, krui_newPattern() );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    deleteAllPatterns
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_deleteAllPatterns(JNIEnv *jEnv, jobject jObj) {
  int i;
  for(i=0; i<NO_OF_PAT_SETS; i++) patternFileNames[i][0] = '\0';
  currentPatternSet = -1;
  krui_deleteAllPatterns();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setShuffle
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setShuffle(JNIEnv *jEnv, jobject jObj, jboolean jMode) {
  isOK(jEnv, krui_shufflePatterns( (bool)jMode ) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setSubShuffle
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setSubShuffle(JNIEnv *jEnv, jobject jObj, jboolean jMode) {
  isOK(jEnv, krui_shuffleSubPatterns( (bool)jMode ) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNoOfPatterns
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNoOfPatterns(JNIEnv *jEnv, jobject jObj) {
  return krui_getNoOfPatterns();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getTotalNoOfSubPatterns
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getTotalNoOfSubPatterns(JNIEnv *jEnv, jobject jObj) {
  return krui_getTotalNoOfSubPatterns();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    allocNewPatternSet
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_allocNewPatternSet(JNIEnv *jEnv, jobject jObj, jstring jPatname) {
  const char *patname = (*jEnv)->GetStringUTFChars(jEnv, jPatname, 0);
  int patnum;
  if( isOK(jEnv, krui_allocNewPatternSet(&patnum)) ) {
    currentPatternSet = patnum;
    strcpy( patternFileNames[patnum], patname );
  }
  (*jEnv)->ReleaseStringUTFChars(jEnv, jPatname, patname);
  return patnum;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setPattern
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setPattern(JNIEnv *jEnv, jobject jObj, jstring jPatternFilename) {
  const char *patternFilename = (*jEnv)->GetStringUTFChars(jEnv, jPatternFilename, 0);
  int patnum = -1;
  
  for (i=0; i<NO_OF_PAT_SETS; i++) { 
    if (! strcmp( patternFileNames[i], patternFilename ) ) { patnum = i; }
  }
  if(patnum == -1) { error(jEnv, "Invalid pattern file name"); }
  if( isOK(jEnv, krui_setCurrPatSet(patnum)) ) currentPatternSet = patnum;
  (*jEnv)->ReleaseStringUTFChars(jEnv, jPatternFilename, patternFilename);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    delPattern
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_delPattern(JNIEnv *jEnv, jobject jObj, jstring jPatternFilename) {
  const char *patternFilename = (*jEnv)->GetStringUTFChars(jEnv, jPatternFilename, 0);
  int patnum = -1;
  
  for (i=0; i<NO_OF_PAT_SETS; i++) { 
    if (! strcmp( patternFileNames[i], patternFilename ) ) { patnum = i; }
  }
  (*jEnv)->ReleaseStringUTFChars(jEnv, jPatternFilename, patternFilename);

  if ( patnum == -1 ) { error(jEnv, "invalid pattern file name"); }
  if( !isOK(jEnv, krui_deletePatSet(patnum)) ) return;
  
  /* Is this OK? Or does deletePatSet influence which pattern set is the current one? */
  if(currentPatternSet >= patnum) currentPatternSet--;

  /* shift all patterns above the deleted to fill the gap */
  while ( patnum < NO_OF_PAT_SETS && patternFileNames[++patnum][0] != 0 ) { 
    strcpy( patternFileNames[patnum-1], patternFileNames[patnum] );
  } 
  strcpy( patternFileNames[patnum-1], "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getPatInfo
 * Signature: ()Ljavanns/KernelInterface$KernelPatternInfo;
 */
JNIEXPORT jobject JNICALL
Java_javanns_KernelInterface_getPatInfo(JNIEnv *jEnv, jobject jObj) {
  jsize len;
  jfieldID fid;
  jintArray jia;
  jint *ibody;
  jdoubleArray jda;
  jdouble *dbody;
  jobjectArray joa;
  pattern_set_info psi;
  pattern_descriptor pd;
  jsize i;
  jclass tmpcls; /* needed for creating a new String array */

  jclass    cls;
  jmethodID mid;
  jobject   jKPI;

  cls  = (*jEnv)->FindClass(jEnv, "javanns/KernelInterface$KernelPatternInfo");
  mid  = (*jEnv)->GetMethodID(jEnv, cls, "<init>", "()V"); 
  jKPI = (*jEnv)->NewObject(jEnv, cls, mid); 

  /* isOK(jEnv, krui_GetPatInfo(&psi, &pd)); */
    if(krui_GetPatInfo(&psi, &pd) < 0) return jKPI;

  /* Pattern Set Info */
  fid = (*jEnv)->GetFieldID(jEnv, cls, "number_of_pattern", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jKPI, fid, psi.number_of_pattern);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "virtual_no_of_pattern", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jKPI, fid, psi.virtual_no_of_pattern);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "output_present", "Z");
  if(fid) (*jEnv)->SetBooleanField(jEnv, jKPI, fid, (jboolean)psi.output_present);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "fixed_fixsizes", "Z");
  if(fid) (*jEnv)->SetBooleanField(jEnv, jKPI, fid, (jboolean)psi.fixed_fixsizes);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "in_fixsize", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jKPI, fid, psi.in_fixsize);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "out_fixsize", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jKPI, fid, psi.out_fixsize);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "in_number_of_dims", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jKPI, fid, psi.in_number_of_dims);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "out_number_of_dims", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jKPI, fid, psi.out_number_of_dims);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "in_max_dim_sizes", "[I");
  if(fid) {
    jia = (jintArray)((*jEnv)->GetObjectField(jEnv, jKPI, fid));
    ibody = (*jEnv)->GetIntArrayElements(jEnv, jia, 0);
    for(i=0; i<psi.in_number_of_dims; i++) ibody[i] = psi.in_max_dim_sizes[i];
    (*jEnv)->ReleaseIntArrayElements(jEnv, jia, ibody, 0);
  }
  fid = (*jEnv)->GetFieldID(jEnv, cls, "out_max_dim_sizes", "[I");
  if(fid) {
    jia = (jintArray)((*jEnv)->GetObjectField(jEnv, jKPI, fid));
    ibody = (*jEnv)->GetIntArrayElements(jEnv, jia, 0);
    for(i=0; i<psi.out_number_of_dims; i++) ibody[i] = psi.out_max_dim_sizes[i];
    (*jEnv)->ReleaseIntArrayElements(jEnv, jia, ibody, 0);
  }
  fid = (*jEnv)->GetFieldID(jEnv, cls, "in_min_dim_sizes", "[I");
  if(fid) {
    jia = (jintArray)((*jEnv)->GetObjectField(jEnv, jKPI, fid));
    ibody = (*jEnv)->GetIntArrayElements(jEnv, jia, 0);
    for(i=0; i<psi.in_number_of_dims; i++) ibody[i] = psi.in_min_dim_sizes[i];
    (*jEnv)->ReleaseIntArrayElements(jEnv, jia, ibody, 0);
  }
  fid = (*jEnv)->GetFieldID(jEnv, cls, "out_min_dim_sizes", "[I");
  if(fid) {
    jia = (jintArray)((*jEnv)->GetObjectField(jEnv, jKPI, fid));
    ibody = (*jEnv)->GetIntArrayElements(jEnv, jia, 0);
    for(i=0; i<psi.out_number_of_dims; i++) ibody[i] = psi.out_min_dim_sizes[i];
    (*jEnv)->ReleaseIntArrayElements(jEnv, jia, ibody, 0);
  }
  fid = (*jEnv)->GetFieldID(jEnv, cls, "classes", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jKPI, fid, psi.classes);
  if(psi.classes) {
    fid = (*jEnv)->GetFieldID(jEnv, cls, "class_names", "[Ljava/lang/String;");
    if(fid) {
      joa = (jobjectArray)( (*jEnv)->GetObjectField(jEnv, jKPI, fid) );
      /* we assume here that joa is not NULL.
         It's Java's responsibility to create the array. */
      len = (*jEnv)->GetArrayLength(jEnv, jia);
      if(len != psi.classes) {
        tmpcls  = (*jEnv)->FindClass(jEnv, "Ljava/lang/String;");
        joa = (*jEnv)->NewObjectArray(jEnv, psi.classes, tmpcls, NULL);
        (*jEnv)->SetObjectField(jEnv, jKPI, fid, joa);
      }
      for(i=0; i<psi.classes; i++) (*jEnv)->SetObjectArrayElement(
        jEnv, joa, i, (*jEnv)->NewStringUTF(jEnv, psi.class_names[i])
        );
    }
  }
  fid = (*jEnv)->GetFieldID(jEnv, cls, "class_distrib_active", "Z");
  if(fid) (*jEnv)->SetBooleanField(jEnv, jKPI, fid, (jboolean)psi.class_distrib_active);
  if(psi.classes && psi.class_distrib_active) {
    fid = (*jEnv)->GetFieldID(jEnv, cls, "class_redistribution", "[I");
    if(fid) {
      jia = (jintArray)((*jEnv)->GetObjectField(jEnv, jKPI, fid));
      /* we assume here that jia is not NULL.
         It's Java's responsibility to create the array. */
      len = (*jEnv)->GetArrayLength(jEnv, jia);
      if(len != psi.classes) {
        jia = (*jEnv)->NewIntArray(jEnv, psi.classes);
        (*jEnv)->SetObjectField(jEnv, jKPI, fid, jia);
      }
      ibody = (*jEnv)->GetIntArrayElements(jEnv, jia, 0);
      for(i=0; i<psi.classes; i++) {
        ibody[i] = psi.class_redistribution[i];
      }
      (*jEnv)->ReleaseIntArrayElements(jEnv, jia, ibody, 0);
    }
  }
  fid = (*jEnv)->GetFieldID(jEnv, cls, "remap_function", "Ljava/lang/String;");
  if(fid) {
    if(psi.remap_function) (*jEnv)->SetObjectField(
      jEnv, jKPI, fid, (*jEnv)->NewStringUTF(jEnv, psi.remap_function)
    );
    else (*jEnv)->SetObjectField(
      jEnv, jKPI, fid, (*jEnv)->NewStringUTF(jEnv, "")
    );
  }
  fid = (*jEnv)->GetFieldID(jEnv, cls, "remap_params", "[D");
  if(fid) {
    jda = (jdoubleArray)((*jEnv)->GetObjectField(jEnv, jKPI, fid));
    dbody = (*jEnv)->GetDoubleArrayElements(jEnv, jda, 0);
    for(i=0; i<psi.no_of_remap_params; i++) dbody[i] = psi.remap_params[i];
    (*jEnv)->ReleaseDoubleArrayElements(jEnv, jda, dbody, 0);
  }
  fid = (*jEnv)->GetFieldID(jEnv, cls, "no_of_remap_params", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jKPI, fid, psi.no_of_remap_params);

  /* Pattern Descriptor */
  fid = (*jEnv)->GetFieldID(jEnv, cls, "input_dim", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jKPI, fid, pd.input_dim);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "input_dim_sizes", "[I");
  if(fid) {
    jia = (jintArray)((*jEnv)->GetObjectField(jEnv, jKPI, fid));
    ibody = (*jEnv)->GetIntArrayElements(jEnv, jia, 0);
    for(i=0; i<pd.input_dim; i++) ibody[i] = pd.input_dim_sizes[i];
    (*jEnv)->ReleaseIntArrayElements(jEnv, jia, ibody, 0);
  }
  fid = (*jEnv)->GetFieldID(jEnv, cls, "input_fixsize", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jKPI, fid, pd.input_fixsize);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "output_dim", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jKPI, fid, pd.output_dim);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "output_dim_sizes", "[I");
  if(fid) {
    jia = (jintArray)((*jEnv)->GetObjectField(jEnv, jKPI, fid));
    ibody = (*jEnv)->GetIntArrayElements(jEnv, jia, 0);
    for(i=0; i<pd.output_dim; i++) ibody[i] = pd.output_dim_sizes[i];
    (*jEnv)->ReleaseIntArrayElements(jEnv, jia, ibody, 0);
  }
  fid = (*jEnv)->GetFieldID(jEnv, cls, "output_fixsize", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jKPI, fid, pd.output_fixsize);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "my_class", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jKPI, fid, pd.my_class);

  return jKPI;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    defShowSubPat
 * Signature: ([I[I[I[I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_defShowSubPat(JNIEnv *jEnv, jobject jObj,
                                   jintArray jInSize, jintArray jOutSize,
                                   jintArray jInPos,  jintArray jOutPos) {
  jsize len;
  jint *body;
  int *inSize, *outSize, *inPos, *outPos;
  jsize i;
  
  // copy jintArray elements into C array, because int size isn't
  // guaranteed to be same in Java in C
  len = (*jEnv)->GetArrayLength(jEnv, jInSize);
  inSize = (int *) malloc(len * sizeof(int));
  if(!inSize) { error(jEnv, "Malloc failed!"); return; }
  body = (*jEnv)->GetIntArrayElements(jEnv, jInSize, 0);
  for(i=0; i<len; i++) inSize[i] = body[i];
  (*jEnv)->ReleaseIntArrayElements(jEnv, jInSize, body, 0);

  len = (*jEnv)->GetArrayLength(jEnv, jOutSize);
  outSize = (int *) malloc(len * sizeof(int));
  if(!outSize) { error(jEnv, "Malloc failed!"); free(inSize); return; }
  body = (*jEnv)->GetIntArrayElements(jEnv, jOutSize, 0);
  for(i=0; i<len; i++) outSize[i] = body[i];
  (*jEnv)->ReleaseIntArrayElements(jEnv, jOutSize, body, 0);

  len = (*jEnv)->GetArrayLength(jEnv, jInPos);
  inPos = (int *) malloc(len * sizeof(int));
  if(!inPos) { error(jEnv, "Malloc failed!"); free(inSize); free(outSize); return; }
  body = (*jEnv)->GetIntArrayElements(jEnv, jInPos, 0);
  for(i=0; i<len; i++) inPos[i] = body[i];
  (*jEnv)->ReleaseIntArrayElements(jEnv, jInPos, body, 0);

  len = (*jEnv)->GetArrayLength(jEnv, jOutPos);
  outPos = (int *) malloc(len * sizeof(int));
  if(!outPos) { error(jEnv, "Malloc failed!"); free(inSize); free(outSize); free(inPos); return; }
  body = (*jEnv)->GetIntArrayElements(jEnv, jOutPos, 0);
  for(i=0; i<len; i++) outPos[i] = body[i];
  (*jEnv)->ReleaseIntArrayElements(jEnv, jOutPos, body, 0);

  isOK(jEnv, krui_DefShowSubPat(inSize, outSize, inPos, outPos));

  free(inSize); free(outSize); free(inPos); free(outPos);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    defTrainSubPat
 * Signature: ([I[I[I[I)I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_defTrainSubPat(JNIEnv *jEnv, jobject jObj,
                                    jintArray jInSize, jintArray jOutSize,
                                    jintArray jInStep, jintArray jOutStep) {
  int max_n_pos;
  jsize len;
  jint *body;
  int *inSize, *outSize, *inStep, *outStep;
  jsize i;
  
  // copy jintArray elements into C array, because int size isn't
  // guaranteed to be same in Java in C
  len = (*jEnv)->GetArrayLength(jEnv, jInSize);
  inSize = (int *) malloc(len * sizeof(int));
  if(!inSize) { error(jEnv, "Malloc failed!"); return -1; }
  body = (*jEnv)->GetIntArrayElements(jEnv, jInSize, 0);
  for(i=0; i<len; i++) inSize[i] = body[i];
  (*jEnv)->ReleaseIntArrayElements(jEnv, jInSize, body, 0);

  len = (*jEnv)->GetArrayLength(jEnv, jOutSize);
  outSize = (int *) malloc(len * sizeof(int));
  if(!outSize) { error(jEnv, "Malloc failed!"); free(inSize); return -1; }
  body = (*jEnv)->GetIntArrayElements(jEnv, jOutSize, 0);
  for(i=0; i<len; i++) outSize[i] = body[i];
  (*jEnv)->ReleaseIntArrayElements(jEnv, jOutSize, body, 0);

  len = (*jEnv)->GetArrayLength(jEnv, jInStep);
  inStep = (int *) malloc(len * sizeof(int));
  if(!inStep) { error(jEnv, "Malloc failed!"); free(inSize); free(outSize); return -1; }
  body = (*jEnv)->GetIntArrayElements(jEnv, jInStep, 0);
  for(i=0; i<len; i++) inStep[i] = body[i];
  (*jEnv)->ReleaseIntArrayElements(jEnv, jInStep, body, 0);

  len = (*jEnv)->GetArrayLength(jEnv, jOutStep);
  outStep = (int *) malloc(len * sizeof(int));
  if(!outStep) { error(jEnv, "Malloc failed!"); free(inSize); free(outSize); free(inStep); return -1; }
  body = (*jEnv)->GetIntArrayElements(jEnv, jOutStep, 0);
  for(i=0; i<len; i++) outStep[i] = body[i];
  (*jEnv)->ReleaseIntArrayElements(jEnv, jOutStep, body, 0);

  isOK(jEnv, krui_DefTrainSubPat(inSize, outSize, inStep, outStep, &max_n_pos));

  free(inSize); free(outSize); free(inStep); free(outStep);
  return max_n_pos;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    alignSubPat
 * Signature: ([I[I)I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_alignSubPat(JNIEnv *jEnv, jobject jObj,
                                 jintArray jInPos, jintArray jOutPos) {
  int n;
  jsize len;
  jint *body;
  int *inPos, *outPos;
  jsize i;
  
  len = (*jEnv)->GetArrayLength(jEnv, jInPos);
  inPos = (int *) malloc(len * sizeof(int));
  if(!inPos) { error(jEnv, "Malloc failed!"); return -1; }
  body = (*jEnv)->GetIntArrayElements(jEnv, jInPos, 0);
  for(i=0; i<len; i++) inPos[i] = body[i];
  (*jEnv)->ReleaseIntArrayElements(jEnv, jInPos, body, 0);

  len = (*jEnv)->GetArrayLength(jEnv, jOutPos);
  outPos = (int *) malloc(len * sizeof(int));
  if(!outPos) { error(jEnv, "Malloc failed!"); free(inPos); return -1; }
  body = (*jEnv)->GetIntArrayElements(jEnv, jOutPos, 0);
  for(i=0; i<len; i++) outPos[i] = body[i];
  (*jEnv)->ReleaseIntArrayElements(jEnv, jOutPos, body, 0);

  isOK(jEnv, krui_AlignSubPat(inPos, outPos, &n));
  free(inPos); free(outPos);
  return n;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getShapeOfSubPattern
 * Signature: (I)Ljavanns/KernelInterface$KernelSubPatShape;
 */
JNIEXPORT jobject JNICALL
Java_javanns_KernelInterface_getShapeOfSubPattern(JNIEnv *jEnv, jobject jObj,
                                          jint jNPos) {
  jfieldID fid;
  jsize len;
  jintArray jia;
  jint *ibody;
  int inSize[MAX_NO_OF_VAR_I_DIM], outSize[MAX_NO_OF_VAR_O_DIM],
      inPos[MAX_NO_OF_VAR_I_DIM],  outPos[MAX_NO_OF_VAR_O_DIM];
  jsize i;

  jclass    cls   = (*jEnv)->FindClass(jEnv, "javanns/KernelInterface$KernelSubPatShape");
  jmethodID mid   = (*jEnv)->GetMethodID(jEnv, cls, "<init>", "()V"); 
  jobject   jKSPS = (*jEnv)->NewObject(jEnv, cls, mid); 

  isOK(jEnv, krui_GetShapeOfSubPattern(inSize, outSize, inPos, outPos, jNPos));

  fid = (*jEnv)->GetFieldID(jEnv, cls, "insize", "[I");
  if(fid) {
    jia = (jintArray)((*jEnv)->GetObjectField(jEnv, jKSPS, fid));
    len = (*jEnv)->GetArrayLength(jEnv, jia);
    ibody = (*jEnv)->GetIntArrayElements(jEnv, jia, 0);
    for(i=0; i<len; i++) ibody[i] = inSize[i];
    (*jEnv)->ReleaseIntArrayElements(jEnv, jia, ibody, 0);
  }
  fid = (*jEnv)->GetFieldID(jEnv, cls, "outsize", "[I");
  if(fid) {
    jia = (jintArray)((*jEnv)->GetObjectField(jEnv, jKSPS, fid));
    len = (*jEnv)->GetArrayLength(jEnv, jia);
    ibody = (*jEnv)->GetIntArrayElements(jEnv, jia, 0);
    for(i=0; i<len; i++) ibody[i] = outSize[i];
    (*jEnv)->ReleaseIntArrayElements(jEnv, jia, ibody, 0);
  }
  fid = (*jEnv)->GetFieldID(jEnv, cls, "inpos", "[I");
  if(fid) {
    jia = (jintArray)((*jEnv)->GetObjectField(jEnv, jKSPS, fid));
    len = (*jEnv)->GetArrayLength(jEnv, jia);
    ibody = (*jEnv)->GetIntArrayElements(jEnv, jia, 0);
    for(i=0; i<len; i++) ibody[i] = inPos[i];
    (*jEnv)->ReleaseIntArrayElements(jEnv, jia, ibody, 0);
  }
  fid = (*jEnv)->GetFieldID(jEnv, cls, "outpos", "[I");
  if(fid) {
    jia = (jintArray)((*jEnv)->GetObjectField(jEnv, jKSPS, fid));
    len = (*jEnv)->GetArrayLength(jEnv, jia);
    ibody = (*jEnv)->GetIntArrayElements(jEnv, jia, 0);
    for(i=0; i<len; i++) ibody[i] = outPos[i];
    (*jEnv)->ReleaseIntArrayElements(jEnv, jia, ibody, 0);
  }

  return jKSPS;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setClassDistribution
 * Signature: ([I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setClassDistribution(JNIEnv *jEnv, jobject jObj,
                                          jintArray jClassSizes) {
  jsize len;
  jint *body;
  unsigned int *cs;
  jsize i;
  
  len = (*jEnv)->GetArrayLength(jEnv, jClassSizes);
  cs = (int *) malloc(len * sizeof(int));
  if(!cs) { error(jEnv, "Malloc failed!"); return; }
  body = (*jEnv)->GetIntArrayElements(jEnv, jClassSizes, 0);
  for(i=0; i<len; i++) cs[i] = body[i];
  (*jEnv)->ReleaseIntArrayElements(jEnv, jClassSizes, body, 0);

  isOK(jEnv, krui_setClassDistribution(cs));
  free(cs);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setClassInfo
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setClassInfo(JNIEnv *jEnv, jobject jObj, jstring jName) {
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jName, 0);
  int i = krui_setClassInfo( (char *)name) ;
  (*jEnv)->ReleaseStringUTFChars(jEnv, jName, name);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    useClassDistribution
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_useClassDistribution(JNIEnv *jEnv, jobject jObj, jboolean flag) {
  isOK(jEnv, krui_useClassDistribution( (bool)flag ) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    loadNet
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_loadNet(JNIEnv *jEnv, jobject jObj, jstring jFilename) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  const char *filename = (*jEnv)->GetStringUTFChars(jEnv, jFilename, 0);
  char *netname;
  
  isOK(jEnv, krui_loadNet( (char *)filename, &netname) );
  cyclenum = 0;
  fid = (*jEnv)->GetFieldID(jEnv, cls, "cycles", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, cyclenum);
  init_net_flag = FALSE; /* maybe the user wants a new init function? */

  (*jEnv)->ReleaseStringUTFChars(jEnv, jFilename, filename);
  if(netname) return (*jEnv)->NewStringUTF(jEnv, netname);
  else return (*jEnv)->NewStringUTF(jEnv, "");
}

/*
 * Class:     javanns_KernelInterface
 * Method:    saveNet
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_saveNet(JNIEnv *jEnv, jobject jObj,
                             jstring jFilename,
                             jstring jNetname) {
  const char *filename = (*jEnv)->GetStringUTFChars(jEnv, jFilename, 0);
  const char *netname = (*jEnv)->GetStringUTFChars(jEnv, jNetname, 0);

  isOK(jEnv, krui_saveNet( (char *)filename, (char *)netname) );

  (*jEnv)->ReleaseStringUTFChars(jEnv, jNetname, netname);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jFilename, filename);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    loadPattern
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_loadPattern(JNIEnv *jEnv, jobject jObj, jstring jFilename) {
  const char *filename = (*jEnv)->GetStringUTFChars(jEnv, jFilename, 0);
  int patnum;
  
  if( isOK(jEnv, krui_loadNewPatterns( (char *)filename, &patnum)) ) {
    strcpy( patternFileNames[patnum], filename );
    currentPatternSet = patnum;
  }
  (*jEnv)->ReleaseStringUTFChars(jEnv, jFilename, filename);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    savePattern
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_savePattern(JNIEnv *jEnv, jobject jObj,
                                 jstring jFilename,
                                 jstring jPatName) {
  const char *filename = (*jEnv)->GetStringUTFChars(jEnv, jFilename, 0);
  const char *patname = (*jEnv)->GetStringUTFChars(jEnv, jPatName, 0);
  int patnum = -1;
  int i;
  
  for (i=0; i<10; i++) { 
    if (! strcmp( patternFileNames[i], patname ) ) { patnum = i; }
  }
  if ( patnum == -1 ) {
    error(jEnv, "invalid pattern name");
    return;
  }
  
  i = krui_saveNewPatterns( (char *)filename, patnum) ;
  (*jEnv)->ReleaseStringUTFChars(jEnv, jFilename, filename);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jPatName, patname);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    saveResult
 * Signature: (Ljava/lang/String;IIZZLjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_saveResult(JNIEnv *jEnv, jobject jObj,
                                jstring jFilename,
                                jint jStartpat,
                                jint jEndpat,
                                jboolean jInclin,
                                jboolean jInclout,
                                jstring jFile_mode) {
  const char *filename = (*jEnv)->GetStringUTFChars(jEnv, jFilename, 0);
  int startpat = jStartpat;
  int endpat = jEndpat;
  int errorCode = 0;
  int dummy = 0;
  bool inclin = jInclin;
  bool inclout = jInclout;
  const char *file_mode = (*jEnv)->GetStringUTFChars(jEnv, jFile_mode, 0);

  bool create;
  
  if (! strcmp(file_mode, "create" ) )       { create = TRUE;  }
  else if (! strcmp(file_mode, "append" ) )  { create = FALSE; }
  else { error(jEnv, "invalid file mode"); }

  /* initialize update parameter array if not already done: */
  if ( !init_update_flag ) {
    for(i=0; i<NO_OF_UPDATE_PARAMS; i++) update[i] = 0.0;
    init_update_flag = TRUE;
  }

  errorCode = krui_saveResultParam(
      (char *)filename, (bool) create, startpat, endpat, inclin, inclout, update, updatenum
  );
  if(errorCode == KRERR_NP_NO_TRAIN_SCHEME) {  
    for(i=0; i<MAX_NO_OF_VAR_DIM; i++) {
      spIsize[i] = 0; spIstep[i] = 0;
      spOsize[i] = 0; spOstep[i] = 0;
    }
    init_subPat_flag = TRUE;
    errorCode = krui_DefTrainSubPat(spIsize, spOsize, spIstep, spOstep, &dummy);
    isOK(jEnv, errorCode);
    if(!errorCode) isOK(
      jEnv,
      krui_saveResultParam(
        (char *)filename, (bool) create, startpat, endpat, inclin, inclout, update, updatenum
      )
    );
  }

  (*jEnv)->ReleaseStringUTFChars(jEnv, jFile_mode, file_mode);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jFilename, filename);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    symbolSearch
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL
Java_javanns_KernelInterface_symbolSearch(JNIEnv *jEnv, jobject jObj,
                                  jstring jSymbol,
                                  jint type) {
  const char *symbol = (*jEnv)->GetStringUTFChars(jEnv, jSymbol, 0);
  jboolean b = krui_symbolSearch( (char *)symbol, type);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jSymbol, symbol);
  return b;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getVersion
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getVersion(JNIEnv *jEnv, jobject jObj) {
  char *p = krui_getVersion();
  if(p) return (*jEnv)->NewStringUTF(jEnv, p );
  else return (*jEnv)->NewStringUTF(jEnv, "" );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getUnitDefaults
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_getUnitDefaults(JNIEnv *jEnv, jobject jObj) {
  float act;
  float bias;
  int ioType;
  int subnetNo;
  int layerNo;
  char actFn[128];
  char *p_actFn = actFn;
  char outFn[128];
  char *p_outFn = outFn;

  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;

  krui_getUnitDefaults( &act, &bias, &ioType, &subnetNo, &layerNo, &p_actFn, &p_outFn );

  fid = (*jEnv)->GetFieldID(jEnv, cls, "defaultActivation", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, act);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "defaultBias", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, bias);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "defaultIOType", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, ioType);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "defaultSubnet", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, subnetNo);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "defaultLayer", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, layerNo);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "defaultActFunction", "Ljava/lang/String;");
  if(fid) (*jEnv)->SetObjectField(jEnv, jObj, fid, (*jEnv)->NewStringUTF(jEnv, actFn ) );
  fid = (*jEnv)->GetFieldID(jEnv, cls, "defaultOutFunction", "Ljava/lang/String;");
  if(fid) (*jEnv)->SetObjectField(jEnv, jObj, fid, (*jEnv)->NewStringUTF(jEnv, outFn ) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setUnitDefaults
 * Signature: (DDIIILjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setUnitDefaults(JNIEnv *jEnv, jobject jObj,
                                     jdouble act,
                                     jdouble bias,
                                     jint io,
                                     jint subnet,
                                     jint layer,
                                     jstring jActFn,
                                     jstring jOutFn) {
  const char *actFn = (*jEnv)->GetStringUTFChars(jEnv, jActFn, 0);
  const char *outFn = (*jEnv)->GetStringUTFChars(jEnv, jOutFn, 0);
  int i = krui_setUnitDefaults(act, bias, io, subnet, layer,  (char *)actFn,  (char *)outFn);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jOutFn, outFn);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jActFn, actFn);
  isOK(jEnv, i);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setSeed
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setSeed(JNIEnv *jEnv, jobject jObj, jint jSeed) {
  krui_setSeedNo( (int)jSeed );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNoOfInputUnits
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNoOfInputUnits(JNIEnv *jEnv, jobject jObj) {
  return krui_getNoOfInputUnits();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNoOfOutputUnits
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNoOfOutputUnits(JNIEnv *jEnv, jobject jObj) {
  return krui_getNoOfOutputUnits();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNoOfSpecialInputUnits
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNoOfSpecialInputUnits(JNIEnv *jEnv, jobject jObj) {
  return krui_getNoOfSpecialInputUnits();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNoOfSpecialOutputUnits
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNoOfSpecialOutputUnits(JNIEnv *jEnv, jobject jObj) {
  return krui_getNoOfSpecialOutputUnits();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    resetNet
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_resetNet(JNIEnv *jEnv, jobject jObj) {
  krui_resetNet();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    allocateUnits
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_allocateUnits(JNIEnv *jEnv, jobject jObj, jint n) {
  isOK(jEnv, krui_allocateUnits(n) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    deleteNet
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_deleteNet(JNIEnv *jEnv, jobject jObj) {
  krui_deleteNet();
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getClassNo
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getClassNo(JNIEnv *jEnv, jobject jObj) {
  int i;
  isOK(jEnv, artui_getClassNo( &i ) );
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getN
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getN(JNIEnv *jEnv, jobject jObj) {
  int i;
  isOK(jEnv, artui_getN( &i ) );
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getM
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getM(JNIEnv *jEnv, jobject jObj) {
  int i;
  isOK(jEnv, artui_getM( &i ) );
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNa
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNa(JNIEnv *jEnv, jobject jObj) {
  int i;
  isOK(jEnv, artui_getNa( &i ) );
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getMa
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getMa(JNIEnv *jEnv, jobject jObj) {
  int i;
  isOK(jEnv, artui_getMa( &i ) );
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNb
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNb(JNIEnv *jEnv, jobject jObj) {
  int i;
  isOK(jEnv, artui_getNb( &i ) );
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getMb
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getMb(JNIEnv *jEnv, jobject jObj) {
  int i;
  isOK(jEnv, artui_getMb( &i ) );
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    analyzer_error
 * Signature: (IIIZ)D
 */
JNIEXPORT jdouble JNICALL
Java_javanns_KernelInterface_analyzer_1error(JNIEnv *jEnv, jobject jObj,
                                     jint currPatt,
                                     jint unitNo,
                                     jint errorType,
                                     jboolean average) {

  return krui_NA_Error(currPatt, unitNo, errorType, average);
}




/* From Batchman:                    */

/*
 * Class:     javanns_KernelInterface
 * Method:    setSubPattern
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setSubPattern(JNIEnv *jEnv, jobject jObj, jstring jList_of_params) {
  const char *list_of_params = (*jEnv)->GetStringUTFChars(jEnv, jList_of_params, 0);

  /* Convert param list to array */
  int argc = 0; char *argv[100], *strptr;  
  strptr = strtok( (char *)list_of_params, " ");
  while (strptr != NULL) { argv[argc++] = strptr; strptr = strtok(NULL, " "); }
  
  if (argc < 4 ) { error(jEnv, "at least four arguments expected"); }
  
  /* initialize the 4 subpattern parameter arrays: */
  for(i=0; i<MAX_NO_OF_VAR_DIM; i++) {
    spIsize[i] = 0; spIstep[i] = 0;
    spOsize[i] = 0; spOstep[i] = 0;
  }

  i = 0; k = 0;
  while ( (i < MAX_NO_OF_VAR_DIM) && (k+4 <= argc) ) {
    spIsize[i] = atoi(argv[k++]);		
    spIstep[i] = atoi(argv[k++]);		
    spOsize[i] = atoi(argv[k++]);		
    spOstep[i] = atoi(argv[k++]);		
    i++;
  }
   
  /* notify that the user program has set the params: */
  init_subPat_flag = TRUE;

  (*jEnv)->ReleaseStringUTFChars(jEnv, jList_of_params, list_of_params);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setParallelMode
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setParallelMode(JNIEnv *jEnv, jobject jObj, jboolean jMode) {
  bool mode = jMode;
  isOK(jEnv, krui_setSpecialNetworkType( mode ? NET_TYPE_FF1 : NET_TYPE_GENERAL ) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    setCascadeParams
 * Signature: (DLjava/lang/String;ZZLjava/lang/String;DIIILjava/lang/String;DIILjava/lang/String;[DZ)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_setCascadeParams(JNIEnv *jEnv, jobject jObj,
                                      jdouble jMax_outp_uni_error,
                                      jstring jLearn_func,
                                      jboolean jPrint_covar,
                                      jboolean jPrune_new_hidden,
                                      jstring jMini_func,
                                      jdouble jMin_covar_change,
                                      jint jCand_patience,
                                      jint jMax_no_covar,
                                      jint jMax_no_cand_units,
                                      jstring jActfunc,
                                      jdouble jError_change,
                                      jint jOutput_patience,
                                      jint jMax_no_epochs,
                                      jstring jModification,
                                      jdoubleArray jModParams,
                                      jboolean jCacheUnitAct) {

  const char *learn_func = (*jEnv)->GetStringUTFChars(jEnv, jLearn_func, 0);
  const char *mini_func = (*jEnv)->GetStringUTFChars(jEnv, jMini_func, 0);
  const char *actfunc = (*jEnv)->GetStringUTFChars(jEnv, jActfunc, 0);
  const char *modification = (*jEnv)->GetStringUTFChars(jEnv, jModification, 0);
  jsize len = (*jEnv)->GetArrayLength(jEnv, jModParams);
  jdouble *params = (*jEnv)->GetDoubleArrayElements(jEnv, jModParams, 0);
  jsize i;

  learn[6] = jMax_outp_uni_error;   /* MAX_PIXEL_ERR                 */

  if(!strcmp(learn_func, "Quickprop"))      learn[7] = QUICKPROP;
  else if(!strcmp(learn_func, "Backprop"))  learn[7] = BACKPROP;
  else if(!strcmp(learn_func, "Rprop"))     learn[7] = RPROP;
  else error(jEnv, "Cascade correlation: invalid subordinate learning function.");

  learn[8]  = jPrint_covar;         /* CC_PRINT_ONOFF                */
  learn[9]  = jMin_covar_change;    /* MIN_COVAR_CHANGE              */
  learn[10] = jCand_patience;       /* SPEC_PATIENCE                 */
  learn[11] = jMax_no_covar;        /* MAX_NO_OF_COVAR_UPDATE_CYCLES */
  learn[12] = jMax_no_cand_units;   /* MAX_SPECIAL_UNIT_NUMBER       */

  if(!strcmp(actfunc, "Act_Logistic"))      learn[13] = ASYM_SIGMOID;
  else if(!strcmp(actfunc, "Act_LogSym"))   learn[13] = SYM_SIGMOID;
  else if(!strcmp(actfunc, "Act_TanH"))     learn[13] = TANH;
  else if(!strcmp(actfunc, "Act_Random"))   learn[13] = RANDOM;
  else error(jEnv, "Cascade correlation: invalid activation function.");

  learn[14] = jError_change;        /* MINIMAL_ERROR_CHANGE          */
  learn[15] = jOutput_patience;     /* OUT_PATIEN                    */
  learn[16] = jMax_no_epochs;       /* MAX_NO_ERROR_UPDATE_CYCLES    */
  learn[17] = jPrune_new_hidden;    /* CC_PRUNE_ONOFF                */

  if(!strcmp(mini_func, "SBC"))         learn[20] = SBC;
  else if(!strcmp(mini_func, "AIC"))    learn[20] = AIC;
  else if(!strcmp(mini_func, "CMSEP"))  learn[20] = CMSEP;
  else error(jEnv, "Cascade correlation: invalid minimization function.");

  if(!strcmp(modification, "NO_MOD"))       learn[21] = CC_NO_MOD;
  else if(!strcmp(modification, "SDCC"))    learn[21] = CC_SDCC;
  else if(!strcmp(modification, "LFCC"))    learn[21] = CC_LFCC;
  else if(!strcmp(modification, "RLCC"))    learn[21] = CC_RLCC;
  else if(!strcmp(modification, "ECC"))     learn[21] = CC_ECC;
  else if(!strcmp(modification, "GCC"))     learn[21] = CC_GCC;
  else if(!strcmp(modification, "STAT"))    learn[21] = CC_STAT;
  else error(jEnv, "Cascade correlation: Invalid modification function.");

  for(i=0; i<len; i++) learn[22+i] = params[i];
  for(; i<5; i++) learn[22+i] = 0;
  learn[27] = jCacheUnitAct;

  (*jEnv)->ReleaseDoubleArrayElements(jEnv, jModParams, params, 0);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jModification, modification);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jActfunc, actfunc);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jMini_func, mini_func);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jLearn_func, learn_func);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    testNet
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_testNet(JNIEnv *jEnv, jobject jObj) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;
  int i;
  
  isOK(jEnv,
       krui_testAllPatterns(
         learn, NO_OF_LEARN_PARAMS + NO_OF_CASCADE_PARAMS,
         &result,
         &resultnum
       )
  );
  
  fid = (*jEnv)->GetFieldID(jEnv, cls, "sse", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]);
  i = krui_getTotalNoOfSubPatterns();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "subpatterns", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "mse", "D");
  if(fid) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]/i);
  i = krui_getNoOfOutputUnits();
  fid = (*jEnv)->GetFieldID(jEnv, cls, "output_units", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, i);
  fid = (*jEnv)->GetFieldID(jEnv, cls, "ssepu", "D");
  if(fid) {
    /* set built-in variable SSEPU only if output units are present: */
    if(i != 0) (*jEnv)->SetDoubleField(jEnv, jObj, fid, result[0]/i);
    else (*jEnv)->SetDoubleField(jEnv, jObj, fid, -1);
  }
  return;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    pruneNet
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_pruneNet(JNIEnv *jEnv, jobject jObj) {
  char *netname;
  float max_error,
        net_error;

  max_error = trainFFNet(jEnv, jObj, first_train_cyc);
  if(lastErrorCode != KRERR_NO_ERROR) return;
  max_error = max_error * (1 + max_error_incr / 100);
  if(max_error < accepted_error) max_error = accepted_error;

  if(recreatef &&
     (pruneTmpFile = tempnam("./", "jNNSprn")) == NULL
     ) { error(jEnv, "Pruning: cannot create temporary file"); return; }

  do {
    if(recreatef && !isOK(jEnv, krui_saveNet(pruneTmpFile, "tmpnet") )) break;
    if(!isOK( jEnv, pr_callPrunFunc(PR_ALL_PATTERNS) )) break;
    if(!isOK( jEnv, pr_calcMeanDeviation(PR_ALL_PATTERNS, &net_error) )) break;
    if(net_error > min_error_to_stop) net_error = trainFFNet(jEnv, jObj, retrain_cyc);
    if(lastErrorCode != KRERR_NO_ERROR) break;
  } while(net_error <= max_error);

  if(recreatef) isOK(jEnv, krui_loadNet(pruneTmpFile, &netname) );
  unlink(pruneTmpFile);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    pruneNet_FirstStep
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL
Java_javanns_KernelInterface_pruneNet_1FirstStep(JNIEnv *jEnv, jobject jObj) {
  float max_error;

  max_error = trainFFNet(jEnv, jObj, first_train_cyc);
  if(lastErrorCode != KRERR_NO_ERROR) return -1;
  max_error = max_error * (1 + max_error_incr / 100);
  if(max_error < accepted_error) max_error = accepted_error;

  if(recreatef &&
     (pruneTmpFile = tempnam("./", "jNNSprn")) == NULL
    ) error(jEnv, "Cannot create temporary file");

  return max_error;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    pruneNet_Step
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL
Java_javanns_KernelInterface_pruneNet_1Step(JNIEnv *jEnv, jobject jObj) {
  float net_error;

  if(recreatef && !isOK(jEnv, krui_saveNet(pruneTmpFile, "tmpnet") )) return 1e38;
  if(!isOK( jEnv, pr_callPrunFunc(PR_ALL_PATTERNS) )) return 1e38;
  if(!isOK( jEnv, pr_calcMeanDeviation(PR_ALL_PATTERNS, &net_error) )) return 1e38;
  if(net_error > min_error_to_stop) net_error = trainFFNet(jEnv, jObj, retrain_cyc);
  if(lastErrorCode != KRERR_NO_ERROR) return 1e38;
  return net_error;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    pruneNet_LastStep
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_pruneNet_1LastStep(JNIEnv *jEnv, jobject jObj) {
  char *netname;
  if(recreatef) isOK(jEnv, krui_loadNet(pruneTmpFile, &netname) );
  unlink(pruneTmpFile);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    pruneTrainNet
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_pruneTrainNet(JNIEnv *jEnv, jobject jObj) {
  jclass cls = (*jEnv)->GetObjectClass(jEnv, jObj);
  jfieldID fid;

  trainFFNet(jEnv, jObj, 1 );
  cyclenum++;
  fid = (*jEnv)->GetFieldID(jEnv, cls, "cycles", "I");
  if(fid) (*jEnv)->SetIntField(jEnv, jObj, fid, cyclenum);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    pruneNetNow
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_pruneNetNow(JNIEnv *jEnv, jobject jObj) {
  isOK(jEnv, pr_callPrunFunc(PR_ALL_PATTERNS) );
}

/*
 * Class:     javanns_KernelInterface
 * Method:    delCandUnits
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_delCandUnits(JNIEnv *jEnv, jobject jObj) {
  isOK(jEnv, krui_cc_deleteAllSpecialUnits() );
}




/* extra methods for JavaGUI:     */

/*
 * Class:     javanns_KernelInterface
 * Method:    getCurrPatternSetNo
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getCurrPatternSetNo(JNIEnv *jEnv, jobject jObj) {
  return currentPatternSet;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getNoOfPatternSets
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javanns_KernelInterface_getNoOfPatternSets(JNIEnv *jEnv, jobject jObj) {
  for (i=0; i<NO_OF_PAT_SETS && patternFileNames[i][0]; i++);
  return i;
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getPatternSet
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getPatternSet(JNIEnv *jEnv, jobject jObj, jint setNo) {
  if(
     setNo < 0 ||
     setNo >= Java_javanns_KernelInterface_getNoOfPatternSets(jEnv, jObj)
    ) {
    isOK(jEnv, KRERR_NP_NO_SUCH_PATTERN_SET);
    return (*jEnv)->NewStringUTF(jEnv, "");
  }
  return (*jEnv)->NewStringUTF(jEnv, patternFileNames[setNo]);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    getCurrPatternSet
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javanns_KernelInterface_getCurrPatternSet(JNIEnv *jEnv, jobject jObj) {
  if(currentPatternSet < 0) {
    isOK(jEnv, KRERR_NP_NO_CURRENT_PATTERN_SET);
    return (*jEnv)->NewStringUTF(jEnv, "");
  }
  return (*jEnv)->NewStringUTF(jEnv, patternFileNames[currentPatternSet]);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    renamePatternSet
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_renamePatternSet__ILjava_lang_String_2(JNIEnv *jEnv, jobject jObj,
                                                            jint setNo,
                                                            jstring jNewName) {
  const char *newName;
  if(
     setNo < 0 ||
     setNo >= Java_javanns_KernelInterface_getNoOfPatternSets(jEnv, jObj)
     ) {
    isOK(jEnv, KRERR_NP_NO_SUCH_PATTERN_SET);
    return;
  }
  newName = (*jEnv)->GetStringUTFChars(jEnv, jNewName, 0);
  strcpy(patternFileNames[setNo], newName);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jNewName, newName);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    renamePatternSet
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_renamePatternSet__Ljava_lang_String_2Ljava_lang_String_2(JNIEnv *jEnv, jobject jObj,
                                                            jstring jName,
                                                            jstring jNewName) {
  int i;
  int patnum = -1;
  const char *name = (*jEnv)->GetStringUTFChars(jEnv, jName, 0);
  const char *newName = (*jEnv)->GetStringUTFChars(jEnv, jNewName, 0);

  for(i=0; i<NO_OF_PAT_SETS; i++) if(!strcmp(patternFileNames[i], name)) patnum = i;
  if(i >= 0) strcpy(patternFileNames[patnum], newName);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jNewName, newName);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jName, name);
  if(i < 0) isOK(jEnv, KRERR_NP_NO_SUCH_PATTERN_SET);
}

/*
 * Class:     javanns_KernelInterface
 * Method:    renameCurrPatternSet
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_renameCurrPatternSet(JNIEnv *jEnv, jobject jObj, jstring jNewName) {
  const char *newName = (*jEnv)->GetStringUTFChars(jEnv, jNewName, 0);
  if(currentPatternSet >= 0) strcpy(patternFileNames[currentPatternSet], newName);
  (*jEnv)->ReleaseStringUTFChars(jEnv, jNewName, newName);
  if(currentPatternSet < 0) isOK(jEnv, KRERR_NP_NO_CURRENT_PATTERN_SET);
}


/*
 * Class:     javanns_KernelInterface
 * Method:    resetSNNS
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_javanns_KernelInterface_resetSNNS(JNIEnv *jEnv, jobject jObj) {
  initnum = learnnum = ffLearnnum = updatenum = resultnum = cyclenum = 0;

  init_net_flag = FALSE,     /* init_param_array init'ed or not */
  init_learn_flag = FALSE,   /* learn_param_array init'ed or not */
  init_ffLearn_flag = FALSE, /* ffLearn_param_array init'ed or not */
  init_update_flag = FALSE,  /* update_param_array init'ed or not */
  init_subPat_flag = FALSE;  /* subPattern arrays init'ed or not */

  Java_javanns_KernelInterface_deleteAllPatterns(jEnv, jObj);
}
