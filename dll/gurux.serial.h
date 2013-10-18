//
// --------------------------------------------------------------------------
//  Gurux Ltd
// 
//
//
// Filename:        $HeadURL$
//
// Version:         $Revision$,
//                  $Date$
//                  $Author$
//
// Copyright (c) Gurux Ltd
//
//---------------------------------------------------------------------------
//
//  DESCRIPTION
//
// This file is a part of Gurux Device Framework.
//
// Gurux Device Framework is Open Source software; you can redistribute it
// and/or modify it under the terms of the GNU General Public License 
// as published by the Free Software Foundation; version 2 of the License.
// Gurux Device Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of 
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU General Public License for more details.
//
// More information of Gurux products: http://www.gurux.org
//
// This code is licensed under the GNU General Public License v2. 
// Full text may be retrieved at http://www.gnu.org/licenses/gpl-2.0.txt
//---------------------------------------------------------------------------

#pragma once

#include <string>
using namespace std;

#include "targetver.h"
// Windows Header Files:
#ifdef _WINDOWS
#include <windows.h>
//Linux headers.
#else
#include <string.h> // string function definitions
#include <unistd.h> // UNIX standard function definitions
#include <fcntl.h> // File control definitions
#include <errno.h> // Error number definitions
#include <termios.h> // POSIX terminal control definitions
#include <time.h>   // time calls
#endif

//If this is not found install JDK.
//In Visual Studio check  Project properties: Additional Include Directories from General in C/C++
//In Eclipse check Includes from Tool Settings from C/C++ Build settings.

#include <jni.h>
#include <vector>

#if defined(_WINDOWS)
#ifdef GURUXSERIAL_EXPORTS
#define GURUXSERIAL_API __declspec(dllexport)
#else
#define GURUXSERIAL_API __declspec(dllimport)
#endif

// This class is exported from the gurux.serial.dll
class GURUXSERIAL_API Cguruxserial {
public:
	// TODO: add your methods here.
};

LONG EnumerateSerialPorts(char* deviceName, DWORD maxLen, DWORD index, BOOL bShowAll = TRUE);

int GXGetCommState(HANDLE hWnd, LPDCB DCB);

int GXSetCommState(HANDLE hWnd, LPDCB DCB);

static void ReportError(JNIEnv* env, DWORD err);

#else //LINUX

// http://ulisse.elettra.trieste.it/services/doc/serial/config.html
void GetLinuxSerialPorts(std::vector<std::basic_string<char> > ports);

#endif

void ReportError(JNIEnv* env, const char* pError)
{
	env->FatalError(pError);	
}

extern "C"
JNIEXPORT jobjectArray JNICALL Java_gurux_serial_NativeCode_getPortNames(JNIEnv* env, jclass clazz);

extern "C"
JNIEXPORT jlong JNICALL Java_gurux_serial_NativeCode_openSerialPort(JNIEnv* env, jclass clazz, jstring port);

extern "C"
JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_closeSerialPort(JNIEnv* env, jclass clazz, jlong hComPort);

extern "C"
JNIEXPORT jbyteArray JNICALL Java_gurux_serial_NativeCode_read(JNIEnv* env, jclass clazz, jlong hComPort, jint readTimeout);

extern "C"
JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_write(JNIEnv* env, jclass clazz, jlong hComPort, jbyteArray data, jint writeTimeout);

extern "C"
JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getBaudRate(JNIEnv* env, jclass clazz, jlong hComPort);

extern "C"
JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setBaudRate(JNIEnv* env, jclass clazz, jlong hComPort, jint value);

extern "C"
JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getDataBits(JNIEnv* env, jclass clazz, jlong hComPort);

extern "C"
JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setDataBits(JNIEnv* env, jclass clazz, jlong hComPort, jint value);

extern "C"
JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getParity(JNIEnv* env, jclass clazz, jlong hComPort);

extern "C"
JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setParity(JNIEnv* env, jclass clazz, jlong hComPort, jint value);

extern "C"
JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getStopBits(JNIEnv* env, jclass clazz, jlong hComPort);

extern "C"
JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setStopBits(JNIEnv* env, jclass clazz, jlong hComPort, jint value);

extern "C"
JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setBreakState(JNIEnv* env, jclass clazz, jlong hComPort, jboolean value);

extern "C"
JNIEXPORT jboolean JNICALL Java_gurux_serial_NativeCode_getRtsEnable(JNIEnv* env, jclass clazz, jlong hComPort);

extern "C"
JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setRtsEnable(JNIEnv* env, jclass clazz, jlong hComPort, jboolean value);

extern "C"
JNIEXPORT jboolean JNICALL Java_gurux_serial_NativeCode_getDtrEnable(JNIEnv* env, jclass clazz, jlong hComPort);

extern "C"
JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setDtrEnable(JNIEnv* env, jclass clazz, jlong hComPort, jboolean value);

extern "C"
JNIEXPORT jboolean JNICALL Java_gurux_serial_NativeCode_getDsrHolding(JNIEnv* env, jclass clazz, jlong hComPort);

extern "C"
JNIEXPORT jboolean JNICALL Java_gurux_serial_NativeCode_getCtsHolding(JNIEnv* env, jclass clazz, jlong hComPort);

extern "C"
JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getBytesToRead(JNIEnv* env, jclass clazz, jlong hComPort);

extern "C"
JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getBytesToWrite(JNIEnv* env, jclass clazz, jlong hComPort);

extern "C"
JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getCDHolding(JNIEnv* env, jclass clazz, jlong hComPort);
