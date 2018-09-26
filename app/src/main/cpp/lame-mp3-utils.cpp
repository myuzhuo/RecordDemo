#include <stdio.h>
#include <jni.h>
#include <string.h> /* memset */
#include <unistd.h> /* close */
#include"lamemp3/lame.h"
#define BUFFER_SIZE 8192
static lame_global_flags *lame = NULL;
long nowConvertBytes = 0;
void resetLame() {
    if (lame != NULL) {
        lame_close(lame);
        lame = NULL;
    }
}

void lameInit(jint inSampleRate,
              jint channel, jint mode, jint outSampleRate,
              jint outBitRate, jint quality) {
    resetLame();
    lame = lame_init();
    lame_set_in_samplerate(lame, inSampleRate);
    lame_set_num_channels(lame, channel);
    lame_set_out_samplerate(lame, outSampleRate);
    lame_set_brate(lame, outBitRate);
    lame_set_quality(lame, quality);
    if(mode == 0) { // use CBR
        lame_set_VBR(lame, vbr_default);
    } else if(mode == 1){ //use VBR
        lame_set_VBR(lame, vbr_abr);
    } else{ // use ABR
        lame_set_VBR(lame, vbr_mtrh);
    }
    lame_init_params(lame);
}



extern "C" {
    JNIEXPORT void JNICALL Java_com_example_record_utils_LameUtil_init(JNIEnv *env, jclass type, jint inSampleRate,
                                                                                                                     jint channel, jint mode, jint outSampleRate,
                                                                                                                     jint outBitRate, jint quality) {
                                                                          lameInit(inSampleRate, channel, mode, outSampleRate, outBitRate, quality);
                                                                      }

    JNIEXPORT void JNICALL Java_com_example_record_utils_LameUtil_convertMp3
                                                                  		(JNIEnv * env, jobject obj, jstring jInputPath, jstring jMp3Path) {
                                                                      const char* cInput = env->GetStringUTFChars(jInputPath, 0);
                                                                      const char* cMp3 = env->GetStringUTFChars(jMp3Path, 0);
                                                                      //open input file and output file
                                                                      FILE* fInput = fopen(cInput,"rb");
                                                                      FILE* fMp3 = fopen(cMp3,"wb");
                                                                      short int inputBuffer[BUFFER_SIZE * 2];
                                                                      unsigned char mp3Buffer[BUFFER_SIZE];//You must specified at least 7200
                                                                      int read = 0; // number of bytes in inputBuffer, if in the end return 0
                                                                      int write = 0;// number of bytes output in mp3buffer.  can be 0
                                                                      long total = 0; // the bytes of reading input file
                                                                      nowConvertBytes = 0;
                                                                      //if you don't init lame, it will init lame use the default value
                                                                      if(lame == NULL){
                                                                          lameInit(44100, 2, 0, 44100, 96, 7);
                                                                      }

                                                                      //convert to mp3
                                                                      do{
                                                                          read = static_cast<int>(fread(inputBuffer, sizeof(short int) * 2, BUFFER_SIZE, fInput));
                                                                          total +=  read * sizeof(short int)*2;
                                                                          nowConvertBytes = total;
                                                                          if(read != 0){
                                                                              write = lame_encode_buffer_interleaved(lame, inputBuffer, read, mp3Buffer, BUFFER_SIZE);
                                                                              //write the converted buffer to the file
                                                                              fwrite(mp3Buffer, sizeof(unsigned char), static_cast<size_t>(write), fMp3);
                                                                          }
                                                                          //if in the end flush
                                                                          if(read == 0){
                                                                              lame_encode_flush(lame,mp3Buffer, BUFFER_SIZE);
                                                                          }
                                                                      }while(read != 0);

                                                                      //release resources
                                                                      resetLame();
                                                                      fclose(fInput);
                                                                      fclose(fMp3);
                                                                      env->ReleaseStringUTFChars(jInputPath, cInput);
                                                                      env->ReleaseStringUTFChars(jMp3Path, cMp3);
                                                                      nowConvertBytes = -1;
                                                                  }

    JNIEXPORT jstring  JNICALL Java_com_example_record_utils_LameUtil_getLameVersion(
                                                                  		JNIEnv *env, jobject /* this */) {
                                                                  	return env->NewStringUTF(get_lame_version());
                                                                  }

    JNIEXPORT jlong  JNICALL Java_com_example_record_utils_LameUtil_getConvertBytes(JNIEnv *env, jclass type) {
                                                                  //    LOGD("convert bytes%d", nowConvertBytes);
                                                                      return nowConvertBytes;
                                                                  }
}