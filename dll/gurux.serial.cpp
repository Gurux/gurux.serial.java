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

#include <string.h>
#include "gurux.serial.h"

#if defined(_WINDOWS)
LONG EnumerateSerialPorts(char* deviceName, DWORD maxLen, DWORD index, BOOL bShowAll)
{
    HKEY hKey;
    char ClassName[MAX_PATH] = ""; // Buffer for class name.
    DWORD dwcClassLen = MAX_PATH; // Length of class string.
    DWORD dwcSubKeys; // Number of sub keys.
    DWORD dwcMaxSubKey; // Longest sub key size.
    DWORD dwcMaxClass; // Longest class string.
    DWORD dwcValues; // Number of values for this key.
    char valueName[MAX_PATH] ;
    DWORD dwcValueName = MAX_PATH;
    DWORD dwcMaxValueName; // Longest Value name.
    DWORD dwcMaxValueData; // Longest Value data.
    DWORD dwcSecDesc; // Security descriptor.
    FILETIME ftLastWriteTime; // Last write time.
    DWORD dwType;
    DWORD retValue;
    DWORD cbData;


    // Use RegOpenKeyEx() with the new Registry path to get an open handle
    // to the child key you want to enumerate.
    DWORD retCode = RegOpenKeyEx (HKEY_LOCAL_MACHINE, "HARDWARE\\DEVICEMAP\\SERIALCOMM",
                0, KEY_ENUMERATE_SUB_KEYS | KEY_EXECUTE | KEY_QUERY_VALUE, &hKey);
    
    //If Registry read failed
    if (retCode != ERROR_SUCCESS)
    {
        return -1;
    }

    // Get Class name, Value count.
    RegQueryInfoKey ( hKey, // Key handle.
        ClassName, // Buffer for class name.
        &dwcClassLen, // Length of class string.
        NULL, // Reserved.
        &dwcSubKeys, // Number of sub keys.
        &dwcMaxSubKey, // Longest sub key size.
        &dwcMaxClass, // Longest class string.
        &dwcValues, // Number of values for this key.
        &dwcMaxValueName, // Longest Value name.
        &dwcMaxValueData, // Longest Value data.
        &dwcSecDesc, // Security descriptor.
        &ftLastWriteTime); // Last write time.

    // Enumerate the Key Values
    cbData = maxLen;
    dwcValueName = MAX_PATH;
    valueName[0] = '\0';

    retValue = RegEnumValue(hKey, index, valueName,
                &dwcValueName, NULL, &dwType,
                (BYTE*) deviceName, &cbData);
    RegCloseKey (hKey); // Close the key handle.
    if(dwType != REG_SZ || retValue != (DWORD)ERROR_SUCCESS)
	{
        return -2;
	}
	if(!bShowAll)
	{
		/*
		CComBSTR tmp = L"\\\\.\\";
		tmp += deviceName;
		//Try to open port. Ignore port if it's open failed.
		HANDLE hComPort = CreateFileW(tmp, 
					GENERIC_READ | GENERIC_WRITE, 0, //Exclusive access, 
					NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
		//Return 1 if port is already opened.
		if (hComPort == INVALID_HANDLE_VALUE)
		{
			 return 1;
		}
		CloseHandle(hComPort);
		*/
	}
	return 0;
}

int GXGetCommState(HANDLE hWnd, LPDCB DCB)
{
	ZeroMemory(DCB, sizeof(LPDCB));
	if (!GetCommState(hWnd, DCB))
	{
		DWORD err = GetLastError(); //Save occured error.
		if (err == 995)
		{
			COMSTAT comstat;
			unsigned long RecieveErrors;
			if (!ClearCommError(hWnd, &RecieveErrors, &comstat))
			{
				return GetLastError();
			}
			if (!GetCommState(hWnd, DCB))
			{
				return GetLastError(); //Save occured error.
			}
		}
		else
		{
			//If USB to serial port converters do not implement this.
			if (err != ERROR_INVALID_FUNCTION)
			{
				return err;
			}
		}
	}
	return 0;
}

int GXSetCommState(HANDLE hWnd, LPDCB DCB)
{
	if (!SetCommState(hWnd, DCB))
	{
		DWORD err = GetLastError(); //Save occured error.
		if (err == 995)
		{
			COMSTAT comstat;
			unsigned long RecieveErrors;
			if (!ClearCommError(hWnd, &RecieveErrors, &comstat))
			{
				return GetLastError();
			}
			if (!SetCommState(hWnd, DCB))
			{
				return GetLastError();
			}
		}
		else
		{
			//If USB to serial port converters do not implement this.
			if (err != ERROR_INVALID_FUNCTION)
			{
				return err;
			}
		}
	}
	return 0;
}

void ReportError(JNIEnv* env, DWORD err)
{
	char buff[MAX_PATH];
	::FormatMessageA(FORMAT_MESSAGE_FROM_SYSTEM,
					 NULL,
					 err, 
					 MAKELANGID(LANG_NEUTRAL,SUBLANG_DEFAULT),
					 buff,
					 MAX_PATH - 1,
					 NULL);
	env->FatalError(buff);	
}

#else

#include <termios.h>
#include <unistd.h>
#include <sys/ioctl.h>

void GetLinuxSerialPorts(std::vector<std::basic_string<char> > ports)
{
    FILE* proc_fd=NULL;
    char proc_line[80];

    // maybe we should read and analyse /proc/tty/driver/serial
    // 0: uart:16550A port:3F8 irq:4 tx:0 rx:0
    // 1: uart:16550A port:2F8 irq:3 tx:0 rx:0
    // 2: uart:unknown port:3E8 irq:4

    proc_fd = fopen("/proc/tty/driver/serial","r");
    if (proc_fd == NULL)
    {
		//Mikko "Failed to find any serial ports.";
    }

    char done = 0;
    do
    {
		if (fgets(proc_line, 77, proc_fd) == NULL)
		{
			//If not end of line it is an error.
			if (!feof(proc_fd))
			{
				//Mikko count = -1;
			}
			proc_line[0]='\0';
			done=1;
		}
		else
		{
			if (strstr(proc_line,"unknown") == NULL && strstr(proc_line,"tx:") != NULL)
			{
				printf("match: %s\n",proc_line);
				//Mikko ports.push_back();
			}
		}
    }
    while (!done);
    fclose(proc_fd);
}
#endif

JNIEXPORT jobjectArray JNICALL Java_gurux_serial_NativeCode_getPortNames(JNIEnv* env, jclass clazz)
{
	std::vector<std::basic_string<char> > portItems;
#ifdef _WINDOWS
	char portname[MAX_PATH];
	DWORD count = 0;
	long retCode = 0;	
	while ((retCode = EnumerateSerialPorts(portname, MAX_PATH, count++, FALSE)) > -1)
	{		
		std::basic_string<char> str;
		str.append(portname);
		portItems.push_back(str);
	}	
#else //If Linux
#endif

	jclass stringClass = env->FindClass("java/lang/String");
	jobjectArray ports = env->NewObjectArray((jsize) portItems.size(), stringClass, 0);	
	jsize pos = -1;		
	for(std::vector<std::basic_string<char> >::iterator it = portItems.begin(); it != portItems.end(); ++it)
	{				
		jobject item = env->NewStringUTF(it->c_str());
		env->SetObjectArrayElement(ports, ++pos, item);
	}	
	return ports;
}

JNIEXPORT jlong JNICALL Java_gurux_serial_NativeCode_openSerialPort(JNIEnv* env, jclass clazz, jstring port)
{
	jboolean isCopy;
#if defined(_WINDOWS)
	const char* pPort = env->GetStringUTFChars(port, &isCopy);
	std::string buff("\\\\.\\");
	buff.append(pPort);
	env->ReleaseStringUTFChars(port, pPort);
	COMMCONFIG commConfig = {0};
	DWORD dwSize = sizeof(commConfig);
	commConfig.dwSize = dwSize;
	//This might fail with virtual COM ports are used.
	GetDefaultCommConfigA(pPort, &commConfig, &dwSize);
	//Open serial port for read / write. Port can't share.
	HANDLE hComPort = CreateFileA(buff.c_str(), 
					GENERIC_READ | GENERIC_WRITE, 0, NULL, 
					OPEN_EXISTING, FILE_FLAG_OVERLAPPED, NULL);	
	if (hComPort == INVALID_HANDLE_VALUE)
	{
		ReportError(env, GetLastError());			
	}
	int ret;
	if ((ret = GXSetCommState(hComPort, &commConfig.dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	return (long) hComPort;

#else //#if defined(__LINUX__) && !defined(__CYGWIN__)
	const char* pPort = env->GetStringUTFChars(port, &isCopy);
	std::string buff(pPort);
	env->ReleaseStringUTFChars(port, pPort);

	// file description for the serial port
	int hComPort; 
	// read/write | not controlling term | don't wait for DCD line signal.
	hComPort = open(buff.c_str(), O_RDWR | O_NOCTTY | O_NDELAY);	
	if(hComPort == -1) // if open is unsucessful
	{
		buff.insert(0, "Failed to Open port: ");
		ReportError(env, buff.c_str());
	}
	else
	{
		fcntl(hComPort, F_SETFL, 0);
	}
	return hComPort;
#endif
}

JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_closeSerialPort(JNIEnv* env, jclass clazz, jlong hComPort)
{	
#if defined(_WINDOWS)
	if (!CloseHandle((HANDLE) hComPort))
	{
		ReportError(env, GetLastError());
	}
#else
	int ret = close(hComPort);
	if (ret < 0)
	{
		ReportError(env, "Failed to close port.");
	}
#endif
}

JNIEXPORT jbyteArray JNICALL Java_gurux_serial_NativeCode_read(JNIEnv* env, jclass clazz, jlong hComPort, jint readTimeout)
{
	int readBufferSize = 1;
#if defined(_WINDOWS)
	if (readTimeout < 1)
	{
		readTimeout = INFINITE;
	}
	COMSTAT comstat;
	unsigned long RecieveErrors;
	if (!ClearCommError((HANDLE) hComPort, &RecieveErrors, &comstat))
	{
		DWORD err = GetLastError();
		ReportError(env, err);
	}		
	//Try to read at least one byte.
    if (comstat.cbInQue > 0)
	{
		readBufferSize = comstat.cbInQue;
	}
	OVERLAPPED osRead;
	ZeroMemory(&osRead, sizeof(osRead));
	osRead.hEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	BYTE* pBuff = new BYTE[readBufferSize];
	DWORD NumberOfBytesRead = 0;
	//Try to read some data
	if (!ReadFile((HANDLE) hComPort, pBuff, readBufferSize, &NumberOfBytesRead, &osRead))
	{
		DWORD err = GetLastError();
		if (err != ERROR_IO_PENDING)     
		{
			delete pBuff;
			ReportError(env, err);
		}
		DWORD received = WaitForSingleObject(osRead.hEvent, readTimeout);
		if (received == WAIT_TIMEOUT)
		{
			delete pBuff;
			env->FatalError("Timeout occurred");
		}
		if (received == WAIT_FAILED)
		{
			delete pBuff;
			err = GetLastError();
			//If port is closed.
			if (err != ERROR_OPERATION_ABORTED)
			{
				return env->NewByteArray(0);
			}
			ReportError(env, err);
		}
		//How many bytes we can read...		
		if (!GetOverlappedResult((HANDLE) hComPort, &osRead, &NumberOfBytesRead, TRUE))
		{
			delete pBuff;
			DWORD err = GetLastError();
			//If port is closed.
			if (err == ERROR_OPERATION_ABORTED)
			{
				return env->NewByteArray(0);
			}
			ReportError(env, err);		
		}
	}
	CloseHandle(osRead.hEvent);
	jbyteArray data = env->NewByteArray(NumberOfBytesRead);
	env->SetByteArrayRegion(data, 0, NumberOfBytesRead, (jbyte*) pBuff);
	delete pBuff;
	return data;
#else
	unsigned char* pBuff = new unsigned char[readBufferSize];
    int ret = read(hComPort, pBuff, readBufferSize);
	if (ret < 0)
	{
		delete pBuff;
		ReportError(env, "Read failed.");
	}
	if (ret == 0)
	{
		delete pBuff;
		ReportError(env, "Read failed. Timeout occurred.");
	}
	jbyteArray data = env->NewByteArray(ret);
	env->SetByteArrayRegion(data, 0, ret, (jbyte*) pBuff);
	delete pBuff;
	return data;
#endif
}

JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_write(JNIEnv* env, jclass clazz, jlong hComPort, jbyteArray data, jint writeTimeout)
{
	int len = env->GetArrayLength(data);
#if defined(_WINDOWS)
	if (len != 0)
	{
		if (writeTimeout < 1)
		{
			writeTimeout = INFINITE;
		}
		DWORD NumberOfBytesWrite = 0;
		BYTE* pData = new BYTE[len];		
		env->GetByteArrayRegion(data, 0, len, (jbyte*) pData);
		OVERLAPPED osWrite;
		ZeroMemory(&osWrite, sizeof(osWrite));
		osWrite.hEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
		if(!::WriteFile((HANDLE) hComPort, pData, len, &NumberOfBytesWrite, &osWrite))
		{
			DWORD err = GetLastError();
			if (err != ERROR_IO_PENDING)     
			{
				delete pData;
				ReportError(env, err);
			}
			DWORD received = WaitForSingleObject(osWrite.hEvent, writeTimeout) == 0;
			if (received == WAIT_TIMEOUT)
			{
				delete pData;
				env->FatalError("Timeout occurred");
			}
			if (received == WAIT_FAILED)
			{
				delete pData;
				ReportError(env, err);
			}
		}
		delete pData;
		if(!CloseHandle(osWrite.hEvent))
		{
			DWORD err = GetLastError();
			ReportError(env, err);
		}
	}
#else
		unsigned char* pData = new unsigned char[len];
		env->GetByteArrayRegion(data, 0, len, (jbyte*) pData);
	    int ret = write(hComPort, pData, len);
		delete pData;
		if (ret < 0)
		{
			ReportError(env, "Write failed.");
		}
#endif
}

JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getBaudRate(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WINDOWS)
	int ret;
	DCB dcb;
	if ((ret = GXGetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	return dcb.BaudRate;
#else
	struct termios options; 
    // Get the current options for the serial port.
    int ret = tcgetattr(hComPort, &options);
	if (ret < 0)
	{
		ReportError(env, "Failed to get settings for serial port.");
	}

	// Get input baud rate.
	return cfgetispeed(&options);    
#endif
}

JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setBaudRate(JNIEnv* env, jclass clazz, jlong hComPort, jint value)
{
#if defined(_WINDOWS)
	int ret;
	DCB dcb;
	if ((ret = GXGetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	dcb.BaudRate = value;
	if ((ret = GXSetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
#else
	struct termios options; 
    // Get the current options for the serial port.
    int ret = tcgetattr(hComPort, &options);
	if (ret < 0)
	{
		ReportError(env, "Failed to get settings for serial port.");
	}
	// Set baud rates.
	cfsetispeed(&options, value);    
	cfsetospeed(&options, value);	
	// Apply the settings to the serial port.
	ret = tcsetattr(hComPort, TCSANOW, &options);
	if (ret < 0)
	{
		ReportError(env, "Failed to set settings for serial port.");
	}
#endif
}

JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getDataBits(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WINDOWS)
	int ret;
	DCB dcb;
	if ((ret = GXGetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	return dcb.ByteSize;
#else
	struct termios options; 
    // Get the current options for the serial port.
    int ret = tcgetattr(hComPort, &options);
	if (ret < 0)
	{
		ReportError(env, "getDataBits failed.");
	}
	// 8-bit chars
	if ((options.c_cflag & CS8) != 0)
	{
		return 8;
	}
	// 7-bit chars
	if ((options.c_cflag & CS7) != 0)
	{
		return 7;
	}
	// 6-bit chars
	if ((options.c_cflag & CS6) != 0)
	{
		return 6;
	}
	// 5-bit chars
	if ((options.c_cflag & CS5) != 0)
	{
		return 5;
	}
	ReportError(env, "getDataBits failed. Unknown value");
	return -1;
#endif
}

JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setDataBits(JNIEnv* env, jclass clazz, jlong hComPort, jint value)
{
#if defined(_WINDOWS)
	int ret;
	DCB dcb;
	if ((ret = GXGetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	dcb.ByteSize = (BYTE) value;
	if ((ret = GXSetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
#else
	struct termios options; 
    // Get the current options for the serial port.
    int ret = tcgetattr(hComPort, &options);
	if (ret < 0)
	{
		ReportError(env, "setDataBits failed.");
	}
	options.c_cflag &= ~CSIZE;
	if (value == 8)
	{
		options.c_cflag |= CS8;
	}
	else if (value == 7)
	{
		options.c_cflag |= CS7;
	}
	else if (value == 6)
	{
		options.c_cflag |= CS6;
	}
	else if (value == 5)
	{
		options.c_cflag |= CS5;
	}
	else
	{
		ReportError(env, "setDataBits failed. Invalid value");
	}
#endif
}

JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getParity(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WINDOWS)
	int ret;
	DCB dcb;
	if ((ret = GXGetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	return dcb.Parity;
#else
	struct termios options; 
    // Get the current options for the serial port.
    int ret = tcgetattr(hComPort, &options);
	if (ret < 0)
	{
		ReportError(env, "Failed to get settings for serial port.");
	}
	//Even parity is used.
	if ((options.c_oflag & PARENB) != 0)
	{
		return 2;
	}
	//Odd parity is used.
	if ((options.c_oflag & PARODD) != 0)
	{
		return 1;
	}
	//No parity i used.
	return 0;
#endif
}

JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setParity(JNIEnv* env, jclass clazz, jlong hComPort, jint value)
{
#if defined(_WINDOWS)
	int ret;
	DCB dcb;
	if ((ret = GXGetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	dcb.Parity = (BYTE) value;
	if ((ret = GXSetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
#else
	struct termios options; 
    // Get the current options for the serial port.
    int ret = tcgetattr(hComPort, &options);
	if (ret < 0)
	{
		ReportError(env, "Failed to get settings for serial port.");
	}
	switch ( value)
    {
	case 0://None: disable parity bit.			     
	    options.c_cflag &= ~PARENB;
	    options.c_iflag &= ~INPCK;
	    break;
	case 1: //odd parity 
	    options.c_cflag |= PARENB;
	    options.c_cflag |= PARODD;
	    options.c_iflag &= ~IGNPAR;
	    options.c_iflag |= INPCK;	     
	    break;
	case 2: //even parity
	    options.c_cflag |= PARENB;
	    options.c_cflag &= ~PARODD;
	    options.c_iflag &= ~IGNPAR;
	    options.c_iflag |= INPCK;
	    break;
	case 3:	//Mark. 
	case 4:	//Space. 
		//Use parity but don't test.
	    options.c_cflag |= PARENB;
	    options.c_iflag |= IGNPAR;
	    break;
	default:
		ReportError(env, "setParity failed. Invalid value.");
    }
#endif
}


JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getStopBits(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WINDOWS)
	int ret;
	DCB dcb;
	if ((ret = GXGetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	return dcb.StopBits;
#else
	struct termios options; 
    // Get the current options for the serial port.
    int ret = tcgetattr(hComPort, &options);
	if (ret < 0)
	{
		ReportError(env, "Failed to get settings for serial port.");
	}
	//One stop bit is used.
	if ((options.c_cflag & CSTOPB) == 0)
	{
		return 1;
	}
	//Two stop bits are used.
	else
	{
		return 2;
	}
#endif
}

JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setStopBits(JNIEnv* env, jclass clazz, jlong hComPort, jint value)
{
#if defined(_WINDOWS)
	int ret;
	DCB dcb;
	if ((ret = GXGetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	dcb.StopBits = (BYTE) value;
	if ((ret = GXSetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
#else
	struct termios options; 
    // Get the current options for the serial port.
    int ret = tcgetattr(hComPort, &options);
	if (ret < 0)
	{
		ReportError(env, "Failed to get settings for serial port.");
	}
	//One stop bit is used.
	if (value == 1)
	{
	    options.c_cflag &= ~CSTOPB;
	    tcsetattr(hComPort,TCSANOW,&options);
	}
	//Two stop bits are used.
	else if (value == 2)
	{
	    options.c_cflag |= CSTOPB;
	    tcsetattr(hComPort,TCSANOW,&options);
	}
	else
	{
		ReportError(env, "setStopBits failed. Invalid value");
    }

#endif
}

JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setBreakState(JNIEnv* env, jclass clazz, jlong hComPort, jboolean value)
{
#if defined(_WINDOWS)
	if (value)
	{
		if (!SetCommBreak((HANDLE) hComPort))
		{
			ReportError(env, GetLastError());
		}
	}
	else
	{
		if (!ClearCommBreak((HANDLE) hComPort))
		{
			ReportError(env, GetLastError());
		}
	}
#else
	int status = 0;
	if (value)
	{
		int ret = ioctl(hComPort, TIOCSBRK, &status);
		if (ret < 0)
		{
			ReportError(env, "setBreakState failed.");
		}
	}
	else
	{
		int ret = ioctl(hComPort, TIOCCBRK, &status);
		if (ret < 0)
		{
			ReportError(env, "setBreakState failed.");
		}
	}
#endif
}

JNIEXPORT jboolean JNICALL Java_gurux_serial_NativeCode_getRtsEnable(JNIEnv* env, jclass clazz, jlong hComPort)
{	
#if defined(_WINDOWS)
	int ret;
	DCB dcb;
	if ((ret = GXGetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	return dcb.fRtsControl == RTS_CONTROL_ENABLE;
#else
	int status = 0;
	int ret = ioctl(hComPort, TIOCMGET, &status);
	if (ret < 0)
	{
		ReportError(env, "getRtsEnable failed.");
	}
	return (status & TIOCM_RTS) != 0;
#endif
}

JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setRtsEnable(JNIEnv* env, jclass clazz, jlong hComPort, jboolean value)
{
#if defined(_WINDOWS)
	DWORD tmp;
	if (value)
	{
		tmp = SETRTS;
	}
	else
	{
		tmp = CLRRTS;
	}

	int ret;
	if ((ret = EscapeCommFunction((HANDLE) hComPort, tmp)) != 0)	
	{
		ReportError(env, ret);
	}
#else
	int status = 0;
	int ret = ioctl(hComPort, TIOCMGET, &status);
	if (ret < 0)
	{
		ReportError(env, "setRtsEnable failed.");
	}
	if (value)
	{
        status |= TIOCM_RTS;
	}
	else
	{
        status &= ~TIOCM_RTS;
	}
	ret = ioctl(hComPort, TIOCMSET, &status);
	if (ret < 0)
	{
		ReportError(env, "setRtsEnable failed.");
	}
#endif
}

JNIEXPORT jboolean JNICALL Java_gurux_serial_NativeCode_getDtrEnable(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WINDOWS)
	int ret;
	DCB dcb;
	if ((ret = GXGetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	return dcb.fDtrControl == DTR_CONTROL_ENABLE;
#else
	int status = 0;
	ioctl(hComPort, TIOCMGET, &status);
	return (status & TIOCM_DTR) != 0;
#endif
}

JNIEXPORT void JNICALL Java_gurux_serial_NativeCode_setDtrEnable(JNIEnv* env, jclass clazz, jlong hComPort, jboolean value)
{
#if defined(_WINDOWS)
	DWORD tmp;
	if (value)
	{
		tmp = SETDTR;
	}
	else
	{
		tmp = CLRDTR;
	}

	int ret;
	if ((ret = EscapeCommFunction((HANDLE) hComPort, tmp)) != 0)	
	{
		ReportError(env, ret);
	}
#else
	int status = 0;
	int ret = ioctl(hComPort, TIOCMGET, &status);
	if (ret < 0)
	{
		ReportError(env, "setDtrEnable failed.");
	}
	if (value)
	{
        status |= TIOCM_DTR;
	}
	else
	{
        status &= ~TIOCM_DTR;
	}
	ret = ioctl(hComPort, TIOCMSET, &status);
	if (ret < 0)
	{
		ReportError(env, "setDtrEnable failed.");
	}
#endif
}

JNIEXPORT jboolean JNICALL Java_gurux_serial_NativeCode_getDsrHolding(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WINDOWS)
	COMSTAT comstat;
	unsigned long RecieveErrors;
	if (!ClearCommError((HANDLE) hComPort, &RecieveErrors, &comstat))
	{
		ReportError(env, GetLastError());
	}
	return comstat.fDsrHold != 0;
#else
	int status = 0;
	int ret = ioctl(hComPort, TIOCMGET, &status);
	if (ret < 0)
	{
		ReportError(env, "getDsrHolding failed.");
	}
	return (status & TIOCM_DSR) != 0;
#endif
}

JNIEXPORT jboolean JNICALL Java_gurux_serial_NativeCode_getCtsHolding(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WINDOWS)
	COMSTAT comstat;
	unsigned long RecieveErrors;
	if (!ClearCommError((HANDLE) hComPort, &RecieveErrors, &comstat))
	{
		ReportError(env, GetLastError());
	}
	return comstat.fCtsHold != 0;
#else
	int status = 0;
	int ret = ioctl(hComPort, TIOCMGET, &status);
	if (ret < 0)
	{
		ReportError(env, "getCtsHolding failed.");
	}
	return (status & TIOCM_CTS) != 0;
#endif
}

JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getBytesToRead(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WINDOWS)
	COMSTAT comstat;
	unsigned long RecieveErrors;
	if (!ClearCommError((HANDLE) hComPort, &RecieveErrors, &comstat))
	{
		ReportError(env, GetLastError());
	}
	return comstat.cbInQue;
#else
	int value = 0;
	int ret = ioctl(hComPort, FIONREAD, &value);
	if (ret < 0)
	{
		ReportError(env, "getBytesToRead failed.");
	}
	return value;
#endif
}

JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getBytesToWrite(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WINDOWS)
	COMSTAT comstat;
	unsigned long RecieveErrors;
	if (!ClearCommError((HANDLE) hComPort, &RecieveErrors, &comstat))
	{
		ReportError(env, GetLastError());
	}
	return comstat.cbOutQue;
#else
	int value = 0;
	int ret = 0;//Mikko TODO ioctl(hComPort, FIONWRITE, &value);
	if (ret < 0)
	{
		ReportError(env, "getBytesToWrite failed.");
	}
	return value;
#endif
}

JNIEXPORT jint JNICALL Java_gurux_serial_NativeCode_getCDHolding(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WINDOWS)
	DWORD status = 0;
	if (!GetCommModemStatus((HANDLE) hComPort, &status))
	{
		ReportError(env, GetLastError());
	}
	return (status & MS_RLSD_ON) != 0;
#else
	int status = 0;
	int ret = ioctl(hComPort, TIOCMGET, &status);
	if (ret < 0)
	{
		ReportError(env, "getCDHolding failed.");
	}
	return (status & TIOCM_CD) != 0;
#endif
}
