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

#if defined(_WIN32) || defined(_WIN64)
LONG EnumerateSerialPorts(char* deviceName, DWORD maxLen, DWORD index, bool bShowAll)
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
	DCB->DCBlength = sizeof(DCB);
	if (!GetCommState(hWnd, DCB))
	{
		DWORD err = GetLastError(); //Save occurred error.
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
				return GetLastError(); //Save occurred error.
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
		DWORD err = GetLastError(); //Save occurred error.
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
	jclass exClass = env->FindClass("java/lang/RuntimeException");
	if (exClass == NULL)
	{
		env->FatalError(buff);
	}
	else
	{
		env->ThrowNew(exClass, buff);
	}	
}

#else
/*
#include <termios.h>
#include <unistd.h>
#include <sys/ioctl.h>
*/

#include <termios.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <linux/serial.h>

#include <stdio.h>
#include <sys/signal.h>

#define _POSIX_SOURCE 1 /* POSIX compliant source */


basic_string<char> GetDriver(const basic_string<char>& tty)
{
    struct stat st;
    string devicedir = tty;

    // Append '/device' to the tty-path
    devicedir += "/device";

    // Stat the device dir and handle it if it is a symlink
    if (lstat(devicedir.c_str(), &st)==0 && S_ISLNK(st.st_mode))
    {
        char buffer[256];
        memset(buffer, 0, sizeof(buffer));
        // Append '/driver' and return basename of the target
        devicedir += "/driver";
        if (readlink(devicedir.c_str(), buffer, sizeof(buffer)) > 0)
        {
            return basename(buffer);
        }
    }
    return "";
}

void GetComPort(const string& dir, vector<basic_string<char> >& ports)
{
    // Get the driver the device is using
    string driver = GetDriver(dir);
    struct serial_struct serinfo;
    // Skip devices without a driver
    if (driver.size() > 0)
    {
        string devfile = string("/dev/") + basename(dir.c_str());
        if (driver == "serial")
        {
        	// Try to open the device
			int fd = open(devfile.c_str(), O_RDWR | O_NONBLOCK | O_NOCTTY);
			if (fd >= 0)
			{
				// Get serial_info
				if (ioctl(fd, TIOCGSERIAL, &serinfo)==0)
				{
					// If device type is no PORT_UNKNOWN we accept the port
					if (serinfo.type != PORT_UNKNOWN)
					{
						ports.push_back(devfile);
					}
				}
				close(fd);
			}
        }
    }
}

void GetLinuxSerialPorts(JNIEnv* env, std::vector<std::basic_string<char> >& ports)
{
	int pos;
	struct dirent **namelist;
	const char* sysdir = "/sys/class/tty/";
	// Scan through /sys/class/tty.
	// it contains all tty-devices in the system
	pos = scandir(sysdir, &namelist, NULL, NULL);
	if (pos < 0)
	{
		ReportError(env, "Failed to enumerate serial ports.");
	}
	while (pos--)
	{
		if (strcmp(namelist[pos]->d_name, "..") != 0
				&& strcmp(namelist[pos]->d_name, ".") != 0)
		{
			// Construct full absolute file path
			string devicedir = sysdir;
			devicedir += namelist[pos]->d_name;
			// Register the device
			GetComPort(devicedir, ports);
		}
		free(namelist[pos]);
	}
	free(namelist);
}
#endif

JNIEXPORT jobjectArray JNICALL Java_gurux_io_NativeCode_getPortNames(JNIEnv* env, jclass clazz)
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
	GetLinuxSerialPorts(env, portItems);
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

void signal_handler_IO (int status)
{
printf("received SIGIO signal.\n");
//        wait_flag = FALSE;
}

JNIEXPORT jlong JNICALL Java_gurux_io_NativeCode_openSerialPort(JNIEnv* env, jclass clazz, jstring port, jlongArray closing)
{
	jboolean isCopy;
#if defined(_WIN32) || defined(_WIN64)
	const char* pPort = env->GetStringUTFChars(port, &isCopy);
	std::string port2(pPort);
	std::string buff("\\\\.\\");
	buff.append(pPort);
	env->ReleaseStringUTFChars(port, pPort);
	//Open serial port for read / write. Port can't share.
	HANDLE hComPort = CreateFileA(buff.c_str(), 
					GENERIC_READ | GENERIC_WRITE, 0, NULL, 
					OPEN_EXISTING, FILE_FLAG_OVERLAPPED, NULL);	
	if (hComPort == INVALID_HANDLE_VALUE)
	{
		ReportError(env, GetLastError());			
	}	
	DCB dcb = {0};
	dcb.DCBlength = sizeof(DCB);
	dcb.BaudRate = 9600;
	dcb.fBinary = 1;
	dcb.fDtrControl = DTR_CONTROL_ENABLE;
	dcb.fRtsControl = DTR_CONTROL_ENABLE;
	dcb.fOutX = dcb.fInX = 0;
    //CTS handshaking on output.
	dcb.fOutxCtsFlow = DTR_CONTROL_DISABLE;
    //DSR handshaking on output
	dcb.fOutxDsrFlow = DTR_CONTROL_DISABLE;    
	//Abort all reads and writes on Error.
	dcb.fAbortOnError = 1;
    dcb.ByteSize = 8;
    //Parity = None.
	dcb.Parity = 0;
	//StopBits = 1;
	dcb.StopBits = 0;
	int ret;	
	if ((ret = GXSetCommState(hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	jlong hClosing = (jlong) ::CreateEvent(NULL, TRUE, FALSE, NULL);
	env->SetLongArrayRegion(closing, 0, 1, &hClosing);	
	return (long) hComPort;
#else //#if defined(__LINUX__)
	const char* pPort = env->GetStringUTFChars(port, &isCopy);
	std::string buff(pPort);
	env->ReleaseStringUTFChars(port, pPort);

	// file description for the serial port
	int hComPort; 
	// read/write | not controlling term | don't wait for DCD line signal.
	hComPort = open(buff.c_str(), O_RDWR | O_NOCTTY | O_NONBLOCK);
	if(hComPort == -1) // if open is unsuccessful.
	{
		buff.insert(0, "Failed to Open port: ");
		ReportError(env, buff.c_str());
	}
	else
	{
		if(!isatty(hComPort))
		{
			ReportError(env, "Failed to Open port. This is not a serial port.");
		}

		if ((ioctl(hComPort, TIOCEXCL) == -1))
		{
			ReportError(env, "Failed to Open port. Exclusive access denied.");
		}

		struct termios options;
		memset(&options,0,sizeof(options));
		options.c_iflag=0;
		options.c_oflag=0;
		options.c_cflag=CS8|CREAD|CLOCAL;           // 8n1, see termios.h for more information
		options.c_lflag=0;
		options.c_cc[VMIN] = 1;
		options.c_cc[VTIME] = 5;
		//Set Baud Rates
		cfsetospeed (&options, B9600);
		cfsetispeed (&options, B9600);

		//hardware flow control is used as default.
		//options.c_cflag |= CRTSCTS;
		if (tcsetattr(hComPort, TCSAFLUSH, &options) != 0)
		{
			ReportError(env, "Failed to Open port. tcsetattr failed.");
			//errno
		}
	}
	return hComPort;
#endif
}

JNIEXPORT void JNICALL Java_gurux_io_NativeCode_closeSerialPort(JNIEnv* env, jclass clazz, jlong hComPort, jlong closing)
{	
	if (hComPort != 0)
	{
#if defined(_WIN32) || defined(_WIN64)
	::SetEvent((HANDLE) closing); 	
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
}

JNIEXPORT jbyteArray JNICALL Java_gurux_io_NativeCode_read(JNIEnv* env, jclass clazz, jlong hComPort, jint readTimeout, jlong closing)
{
	int readBufferSize = 1;
#if defined(_WIN32) || defined(_WIN64)
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
			if (err == ERROR_INVALID_HANDLE) 
			{
				CloseHandle((HANDLE) closing);
				return env->NewByteArray(0);
			}						
			ReportError(env, err);
		}
		HANDLE h[2];
		h[0] = osRead.hEvent;
		h[1] = (HANDLE) closing;
		DWORD received = WaitForMultipleObjects(2, h, FALSE, readTimeout);
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
		//If closed.
		if (received == WAIT_OBJECT_0 + 1)
		{
			CloseHandle((HANDLE) closing);
			NumberOfBytesRead = 0;
		}
		//How many bytes we can read...		
		else if (!GetOverlappedResult((HANDLE) hComPort, &osRead, &NumberOfBytesRead, TRUE))
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
	//Get bytes available.
	int ret = ioctl(hComPort, FIONREAD, &readBufferSize);
	if (ret < 0)
	{
		ReportError(env, "getBytesToRead failed.");
	}
	//Try to read at least one byte.
	if (readBufferSize == 0)
	{
		readBufferSize = 1;
	}
	unsigned char* pBuff = new unsigned char[readBufferSize];
    do
    {
		ret = read(hComPort, pBuff, readBufferSize);
		if (ret == -1)
		{
			if (errno == EAGAIN)
			{
				ret = 0;
				usleep(100000);
			}
			//Return empty list if connection is closed.
			else if (errno == EBADF)
			{
				ret = 0;
				break;
			}
			else
			{
				delete pBuff;
				char buff[50];
				sprintf(buff, "Read failed %d", errno);
				ReportError(env, buff);
			}
		}
    }
    while(ret == 0);
	jbyteArray data = env->NewByteArray(ret);
	env->SetByteArrayRegion(data, 0, ret, (jbyte*) pBuff);
	delete pBuff;
	return data;
#endif
}

JNIEXPORT void JNICALL Java_gurux_io_NativeCode_write(JNIEnv* env, jclass clazz, jlong hComPort, jbyteArray data, jint writeTimeout)
{
	int len = env->GetArrayLength(data);
#if defined(_WIN32) || defined(_WIN64)
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
		if (ret != len)
		{
			ReportError(env, "Write failed.");
		}
#endif
}

JNIEXPORT jint JNICALL Java_gurux_io_NativeCode_getBaudRate(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WIN32) || defined(_WIN64)
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
	ret = cfgetispeed(&options);
	switch(ret)
	{
		case 50:
			return 50;
		case 75:
			return 75;
		case 110:
			return 110;
		case 134:
			return 134;
		case 150:
			return 150;
		case 200:
			return 200;
		case 600:
			return 600;
		case 1200:
			return 1200;
		case 1800:
			return 1800;
		case 2400:
			return 2400;
		case 4800:
			return 4800;
		case 9600:
			return 9600;
		case 19200:
			return 19200;
		case 38400:
			return 38400;
	}
	ReportError(env, "Invalid baud rate.");
	return -1;
#endif
}

JNIEXPORT void JNICALL Java_gurux_io_NativeCode_setBaudRate(JNIEnv* env, jclass clazz, jlong hComPort, jint value)
{
#if defined(_WIN32) || defined(_WIN64)
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
	switch(value)
	{
		case 50:
		   value = 50;
		break;
		case 75:
		   value = 75;
		break;
		case 110:
		   value = 110;
		break;
		case 134:
		   value = 134;
		break;
		case 150:
		   value = 150;
		break;
		case 200:
		   value = 200;
		break;
		case 300:
		   value = 300;
		break;
		case 600:
		   value = 600;
		break;
		case 1200:
		   value = 1200;
		break;
		case 1800:
		   value = 1800;
		break;
		case 2400:
		   value = 2400;
		break;
		case 4800:
		   value = 4800;
		break;
		case 9600:
		   value = 9600;
		break;
		case 19200:
		   value = 19200;
		break;
		case 38400:
		   value = 38400;
		break;
		default:
			ReportError(env, "Invalid value.");
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

JNIEXPORT jint JNICALL Java_gurux_io_NativeCode_getDataBits(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WIN32) || defined(_WIN64)
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

JNIEXPORT void JNICALL Java_gurux_io_NativeCode_setDataBits(JNIEnv* env, jclass clazz, jlong hComPort, jint value)
{
#if defined(_WIN32) || defined(_WIN64)
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

JNIEXPORT jint JNICALL Java_gurux_io_NativeCode_getParity(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WIN32) || defined(_WIN64)
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

JNIEXPORT void JNICALL Java_gurux_io_NativeCode_setParity(JNIEnv* env, jclass clazz, jlong hComPort, jint value)
{
#if defined(_WIN32) || defined(_WIN64)
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


JNIEXPORT jint JNICALL Java_gurux_io_NativeCode_getStopBits(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WIN32) || defined(_WIN64)
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

JNIEXPORT void JNICALL Java_gurux_io_NativeCode_setStopBits(JNIEnv* env, jclass clazz, jlong hComPort, jint value)
{
#if defined(_WIN32) || defined(_WIN64)
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

JNIEXPORT void JNICALL Java_gurux_io_NativeCode_setBreakState(JNIEnv* env, jclass clazz, jlong hComPort, jboolean value)
{
#if defined(_WIN32) || defined(_WIN64)
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

JNIEXPORT jboolean JNICALL Java_gurux_io_NativeCode_getRtsEnable(JNIEnv* env, jclass clazz, jlong hComPort)
{	
#if defined(_WIN32) || defined(_WIN64)
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

JNIEXPORT void JNICALL Java_gurux_io_NativeCode_setRtsEnable(JNIEnv* env, jclass clazz, jlong hComPort, jboolean value)
{
#if defined(_WIN32) || defined(_WIN64)
	DWORD tmp;
	if (value)
	{
		tmp = SETRTS;
	}
	else
	{
		tmp = CLRRTS;
	}

	if (EscapeCommFunction((HANDLE) hComPort, tmp) == 0)	
	{
		ReportError(env, GetLastError());
	}
#else
	int status = 0;
	int ret = ioctl(hComPort, TIOCMGET, &status);
	if (ret != 0)
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
	if (ret != 0)
	{
		ReportError(env, "setRtsEnable failed.");
	}
#endif
}

JNIEXPORT jboolean JNICALL Java_gurux_io_NativeCode_getDtrEnable(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WIN32) || defined(_WIN64)
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

JNIEXPORT void JNICALL Java_gurux_io_NativeCode_setDtrEnable(JNIEnv* env, jclass clazz, jlong hComPort, jboolean value)
{
#if defined(_WIN32) || defined(_WIN64)
	DWORD tmp;
	if (value)
	{
		tmp = SETDTR;
	}
	else
	{
		tmp = CLRDTR;
	}
	if (EscapeCommFunction((HANDLE) hComPort, tmp) == 0)	
	{
		ReportError(env, GetLastError());
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

JNIEXPORT jboolean JNICALL Java_gurux_io_NativeCode_getDsrHolding(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WIN32) || defined(_WIN64)
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

JNIEXPORT jboolean JNICALL Java_gurux_io_NativeCode_getCtsHolding(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WIN32) || defined(_WIN64)
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

JNIEXPORT jint JNICALL Java_gurux_io_NativeCode_getBytesToRead(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WIN32) || defined(_WIN64)
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

JNIEXPORT jint JNICALL Java_gurux_io_NativeCode_getBytesToWrite(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WIN32) || defined(_WIN64)
	COMSTAT comstat;
	unsigned long RecieveErrors;
	if (!ClearCommError((HANDLE) hComPort, &RecieveErrors, &comstat))
	{
		ReportError(env, GetLastError());
	}
	return comstat.cbOutQue;
#else
	//There is no FIONWRITE in Linux so return always Zero.
	return 0;
	/*
	int value = 0;
	int ret = ioctl(hComPort, FIONWRITE, &value);
	if (ret < 0)
	{
		ReportError(env, "getBytesToWrite failed in Linux.");
	}
	return value;
	*/
#endif
}

JNIEXPORT jint JNICALL Java_gurux_io_NativeCode_getCDHolding(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WIN32) || defined(_WIN64)
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

JNIEXPORT jint JNICALL Java_gurux_io_NativeCode_getHandshake(JNIEnv* env, jclass clazz, jlong hComPort)
{
#if defined(_WIN32) || defined(_WIN64)
	DCB dcb;
	int ret;
	if ((ret = GXGetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	// Disable DTR monitoring
	if (dcb.fDtrControl == DTR_CONTROL_DISABLE && 
		// Disable RTS (Ready To Send)
		dcb.fRtsControl == DTR_CONTROL_DISABLE)
	{
		// Enable XON/XOFF for transmission
		if (dcb.fOutX && 							
			// Enable XON/XOFF for receiving
			dcb.fInX)
		{
			//XOnXOff
			return 1;
		}
		//None
		return 0;
	}

	// Enable XON/XOFF for transmission
	if (dcb.fOutX && 							
		// Enable XON/XOFF for receiving
		dcb.fInX)
	{
		//RequestToSendXOnXOff
		return 3;
	}
	//hardware flow control is used.
	return 2;
#else
	struct termios options;
	    // Get the current options for the serial port.
	    int ret = tcgetattr(hComPort, &options);
		if (ret < 0)
		{
			ReportError(env, "Failed to get settings for serial port.");
		}
		// Disable DTR monitoring
		if ((options.c_cflag & CRTSCTS) == 0)
		{
			if ((options.c_iflag &  (IXON | IXOFF | IXANY)) == (IXON | IXOFF | IXANY))
			{
				//XOnXOff
				return 1;
			}
			//None
			return 0;
		}
		if ((options.c_iflag &  (IXON | IXOFF | IXANY)) == (IXON | IXOFF | IXANY))
		{
			//RequestToSendXOnXOff
			return 3;
		}
		//hardware flow control is used.
		return 2;
#endif

}

JNIEXPORT void JNICALL Java_gurux_io_NativeCode_setHandshake(JNIEnv* env, jclass clazz, jlong hComPort, jint value)
{	
#if defined(_WIN32) || defined(_WIN64)
	DCB dcb;
	int ret;
	if ((ret = GXGetCommState((HANDLE) hComPort, &dcb)) != 0)	
	{
		ReportError(env, ret);
	}
	//None
	if (value == 0)
	{
		dcb.fDtrControl = DTR_CONTROL_DISABLE;
		// Disable RTS (Ready To Send)
		dcb.fRtsControl = DTR_CONTROL_DISABLE;
		dcb.fOutX = dcb.fInX = 0;
	}
	//XOnXOff
	else if (value == 1)
	{
		dcb.fDtrControl = DTR_CONTROL_DISABLE;
		// Disable RTS (Ready To Send)
		dcb.fRtsControl = DTR_CONTROL_DISABLE;
		dcb.fOutX = dcb.fInX = 1;

	}
	//hardware flow control is used.
	else if (value == 2)
	{
		dcb.fDtrControl = DTR_CONTROL_ENABLE;
		// Disable RTS (Ready To Send)
		dcb.fRtsControl = DTR_CONTROL_ENABLE;
		dcb.fOutX = dcb.fInX = 0;
	}
	//RequestToSendXOnXOff
	else if (value == 3)
	{
		dcb.fDtrControl = DTR_CONTROL_ENABLE;
		// Disable RTS (Ready To Send)
		dcb.fRtsControl = DTR_CONTROL_ENABLE;
		dcb.fOutX = dcb.fInX = 1;
	}
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
	//None
	if (value == 0)
	{
		options.c_cflag &= ~CRTSCTS;       // no flow control
		options.c_iflag &=  ~(IXON | IXOFF | IXANY);// turn off s/w flow control.
	}
	//XOnXOff
	else if (value == 1)
	{
		options.c_cflag &= ~CRTSCTS;       // no flow control
		options.c_iflag |=  (IXON | IXOFF | IXANY);// turn s/w flow control On.

	}
	//hardware flow control is used.
	else if (value == 2)
	{
		options.c_cflag |= CRTSCTS;       // flow control is used.
		options.c_iflag &=  ~(IXON | IXOFF | IXANY);// turn off s/w flow ctcontrolrl
	}
	//RequestToSendXOnXOff
	else if (value == 3)
	{
		options.c_cflag |= CRTSCTS;       // flow control is used.
		options.c_iflag |=  (IXON | IXOFF | IXANY);// turn s/w flow control On.
	}
	if (tcsetattr(hComPort, TCSANOW, &options) != 0)
	{
		ReportError(env, "tcsetattr failed.");
	}
#endif

}
