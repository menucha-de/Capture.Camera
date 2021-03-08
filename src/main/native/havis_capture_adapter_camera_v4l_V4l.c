#include "havis_capture_adapter_camera_v4l_V4l.h"
#include <jni.h>

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#define __USE_POSIX199309
#include <time.h>
#include <sys/mman.h>
#include <linux/videodev2.h>
#include <libv4l2.h>

#define V4L_EXCEPTION "havis/capture/adapter/camera/v4l/V4lException"

// the 'DHT' segment according to RFC 2435
#define DHT_SIZE		420
static jbyte HUFFMAN_TABLES[] =
	{0xFF,0xC4,0x01,0xA2,0x00,0x00,0x01,0x05,0x01,0x01,0x01,0x01
	,0x01,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01,0x02
	,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x01,0x00,0x03
	,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x00,0x00,0x00
	,0x00,0x00,0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09
	,0x0A,0x0B,0x10,0x00,0x02,0x01,0x03,0x03,0x02,0x04,0x03,0x05
	,0x05,0x04,0x04,0x00,0x00,0x01,0x7D,0x01,0x02,0x03,0x00,0x04
	,0x11,0x05,0x12,0x21,0x31,0x41,0x06,0x13,0x51,0x61,0x07,0x22
	,0x71,0x14,0x32,0x81,0x91,0xA1,0x08,0x23,0x42,0xB1,0xC1,0x15
	,0x52,0xD1,0xF0,0x24,0x33,0x62,0x72,0x82,0x09,0x0A,0x16,0x17
	,0x18,0x19,0x1A,0x25,0x26,0x27,0x28,0x29,0x2A,0x34,0x35,0x36
	,0x37,0x38,0x39,0x3A,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0x4A
	,0x53,0x54,0x55,0x56,0x57,0x58,0x59,0x5A,0x63,0x64,0x65,0x66
	,0x67,0x68,0x69,0x6A,0x73,0x74,0x75,0x76,0x77,0x78,0x79,0x7A
	,0x83,0x84,0x85,0x86,0x87,0x88,0x89,0x8A,0x92,0x93,0x94,0x95
	,0x96,0x97,0x98,0x99,0x9A,0xA2,0xA3,0xA4,0xA5,0xA6,0xA7,0xA8
	,0xA9,0xAA,0xB2,0xB3,0xB4,0xB5,0xB6,0xB7,0xB8,0xB9,0xBA,0xC2
	,0xC3,0xC4,0xC5,0xC6,0xC7,0xC8,0xC9,0xCA,0xD2,0xD3,0xD4,0xD5
	,0xD6,0xD7,0xD8,0xD9,0xDA,0xE1,0xE2,0xE3,0xE4,0xE5,0xE6,0xE7
	,0xE8,0xE9,0xEA,0xF1,0xF2,0xF3,0xF4,0xF5,0xF6,0xF7,0xF8,0xF9
	,0xFA,0x11,0x00,0x02,0x01,0x02,0x04,0x04,0x03,0x04,0x07,0x05
	,0x04,0x04,0x00,0x01,0x02,0x77,0x00,0x01,0x02,0x03,0x11,0x04
	,0x05,0x21,0x31,0x06,0x12,0x41,0x51,0x07,0x61,0x71,0x13,0x22
	,0x32,0x81,0x08,0x14,0x42,0x91,0xA1,0xB1,0xC1,0x09,0x23,0x33
	,0x52,0xF0,0x15,0x62,0x72,0xD1,0x0A,0x16,0x24,0x34,0xE1,0x25
	,0xF1,0x17,0x18,0x19,0x1A,0x26,0x27,0x28,0x29,0x2A,0x35,0x36
	,0x37,0x38,0x39,0x3A,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0x4A
	,0x53,0x54,0x55,0x56,0x57,0x58,0x59,0x5A,0x63,0x64,0x65,0x66
	,0x67,0x68,0x69,0x6A,0x73,0x74,0x75,0x76,0x77,0x78,0x79,0x7A
	,0x82,0x83,0x84,0x85,0x86,0x87,0x88,0x89,0x8A,0x92,0x93,0x94
	,0x95,0x96,0x97,0x98,0x99,0x9A,0xA2,0xA3,0xA4,0xA5,0xA6,0xA7
	,0xA8,0xA9,0xAA,0xB2,0xB3,0xB4,0xB5,0xB6,0xB7,0xB8,0xB9,0xBA
	,0xC2,0xC3,0xC4,0xC5,0xC6,0xC7,0xC8,0xC9,0xCA,0xD2,0xD3,0xD4
	,0xD5,0xD6,0xD7,0xD8,0xD9,0xDA,0xE2,0xE3,0xE4,0xE5,0xE6,0xE7
	,0xE8,0xE9,0xEA,0xF2,0xF3,0xF4,0xF5,0xF6,0xF7,0xF8,0xF9,0xFA
	};

#define CLEAR(x) memset(&(x), 0, sizeof(x))

struct buffer {
        void   *start;
        size_t length;
};
        
int camFD = -1;
int width;
int height;
struct buffer imageBuffer;
struct v4l2_buffer buf;
bool init = false;

/*
 * Class:     havis_capture_adapter_camera_v4l_V4l
 * Method:    open
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT void JNICALL Java_havis_capture_adapter_camera_v4l_V4l_open
  (JNIEnv *env, jobject this, jstring device)
{
	const char *deviceString = (*env)->GetStringUTFChars(env, device, false); 
	camFD = open(deviceString, O_RDWR | O_NONBLOCK);
	(*env)->ReleaseStringUTFChars(env, device, deviceString);
}

/*
 * Class:     havis_capture_adapter_camera_v4l_V4l
 * Method:    init
 * Signature: ()I
 */
JNIEXPORT void JNICALL Java_havis_capture_adapter_camera_v4l_V4l_init
  (JNIEnv *env, jobject this)
{
    struct v4l2_requestbuffers	req;
    struct v4l2_format	fmt;
    
    CLEAR(fmt);
    fmt.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    fmt.fmt.pix.width       = width;
    fmt.fmt.pix.height      = height;
    fmt.fmt.pix.pixelformat = V4L2_PIX_FMT_MJPEG;
    fmt.fmt.pix.field       = V4L2_FIELD_INTERLACED;
    if (v4l2_ioctl(camFD, VIDIOC_S_FMT, &fmt) == -1) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "Failed to write VIDIOC_S_FMT");
        return;
    }

    CLEAR(req);
    req.count = 1;
    req.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    req.memory = V4L2_MEMORY_MMAP;
    if (v4l2_ioctl(camFD, VIDIOC_REQBUFS, &req) == -1) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "Failed to read VIDIOC_REQBUFS");
        return;
    }
    
    CLEAR(buf);
	buf.type        = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	buf.memory      = V4L2_MEMORY_MMAP;
	buf.index       = 0;
	if (v4l2_ioctl(camFD, VIDIOC_QUERYBUF, &buf)<0){
		(*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "Failed to read VIDIOC_QUERYBUF");
		return;
	}
	imageBuffer.length = buf.length;	
	imageBuffer.start = v4l2_mmap(NULL, buf.length,
							  PROT_READ | PROT_WRITE, MAP_SHARED,
							  camFD, buf.m.offset);
	if (MAP_FAILED == imageBuffer.start) {
		(*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "MAP_FAILED");
		return;
	}	
}

/*
 * Class:     havis_capture_adapter_camera_v4l_V4l
 * Method:    start
 * Signature: ()I
 */
JNIEXPORT void JNICALL Java_havis_capture_adapter_camera_v4l_V4l_start
  (JNIEnv *env, jobject this)
{
    enum v4l2_buf_type type;
    type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    if (v4l2_ioctl(camFD, VIDIOC_STREAMON, &type) == -1) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "Failed to start VIDIOC_STREAMON");
        return;
    }
}

/*
 * Class:     havis_capture_adapter_camera_v4l_V4l
 * Method:    stop
 * Signature: ()I
 */
JNIEXPORT void JNICALL Java_havis_capture_adapter_camera_v4l_V4l_stop
  (JNIEnv *env, jobject this)
{
	enum v4l2_buf_type type;
    type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    if (v4l2_ioctl(camFD, VIDIOC_STREAMOFF, &type) == -1){
        (*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "Failed to stop VIDIOC_STREAMOFF");
        return;
	};
}

/*
 * Class:     havis_capture_adapter_camera_v4l_V4l
 * Method:    setResolution
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_havis_capture_adapter_camera_v4l_V4l_setResolution
  (JNIEnv *env, jobject this, jint _width, jint _height)
{
	width = _width;
	height = _height;
}

/*
 * Class:     havis_capture_adapter_camera_v4l_V4l
 * Method:    capture
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_havis_capture_adapter_camera_v4l_V4l_capture
  (JNIEnv *env, jobject this)
{
	fd_set fds;
	struct timeval tv;
	    
    if (camFD <= 0) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "CamFD is not set");
        return (*env)->NewGlobalRef(env, NULL);
    }
    
    struct v4l2_buffer buf;
	CLEAR(buf);
    buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	buf.memory = V4L2_MEMORY_MMAP;
    buf.index = 0;
    if (v4l2_ioctl(camFD, VIDIOC_QBUF, &buf) == -1) {
		(*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "Failed to queue video buffer");
		return (*env)->NewGlobalRef(env, NULL);
	}
  
	FD_ZERO(&fds);
	FD_SET(camFD, &fds);
	tv.tv_sec = 2;
	tv.tv_usec = 0;
	if (select(camFD + 1, &fds, NULL, NULL, &tv) <= 0) {
		(*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "Failed to read video buffer in time");
		return (*env)->NewGlobalRef(env, NULL);
	}

	CLEAR(buf);
	buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	buf.memory = V4L2_MEMORY_MMAP;
	if (v4l2_ioctl(camFD, VIDIOC_DQBUF, &buf) == -1) {
		(*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "Failed to dequeue video buffer");
		return (*env)->NewGlobalRef(env, NULL);
	}

	int size = buf.bytesused;
	
	if (size<2){
		(*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "Image size too small");
		return (*env)->NewGlobalRef(env, NULL);
	}
	
	if (size>imageBuffer.length){
		(*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "Image size too big");
		return (*env)->NewGlobalRef(env, NULL);	
	}
	/*
	if(((char*)imageBuffer.start)[0]!=0xFF && ((char*)imageBuffer.start)[1]!=0xD8) {
		return (*env)->NewGlobalRef(env, NULL);
	}
	*/
	
	
	jbyteArray ret = (*env)->NewByteArray(env, size + 2 + DHT_SIZE);
	if (ret == NULL){
		(*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "Can not create buffer array");
		return (*env)->NewGlobalRef(env, NULL);
	}
	
	if (imageBuffer.start != NULL && MAP_FAILED != imageBuffer.start) {
		(*env)->SetByteArrayRegion (env, ret, 0, 2, imageBuffer.start);
		(*env)->SetByteArrayRegion (env, ret, 2, DHT_SIZE, HUFFMAN_TABLES);
		(*env)->SetByteArrayRegion (env, ret, 2+DHT_SIZE, size - 2, &((jbyte*)imageBuffer.start)[2]);
	} else {
		(*env)->ThrowNew(env, (*env)->FindClass(env, V4L_EXCEPTION), "Image Buffer failure");
		return (*env)->NewGlobalRef(env, NULL);
	}
	
	return ret;
}

/*
 * Class:     havis_capture_adapter_camera_v4l_V4l
 * Method:    close
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_havis_capture_adapter_camera_v4l_V4l_close
  (JNIEnv *env, jobject this)
{
	if (camFD>=0){
		v4l2_munmap(imageBuffer.start, imageBuffer.length);
		close(camFD);
		camFD = -1;
	}
}

