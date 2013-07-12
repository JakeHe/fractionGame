/*
 *
 *  Bluetooth BLUEZ NATIVE interface for ANDROID
 *
 *  Copyright (C) 2006-2010 Radu Motisan
 *  
 *  www.pocketmagic.net
 *
 *  All rights reserved. 
 *  To copy or distribute this source code or any compiled code resulted from using this source code, 
 *  you need the author's written approval.
 */ 

#include <string.h>
#include <jni.h>
#include <sys/ioctl.h>

#include "btutil.h"
#include "hci.h"




jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
	//native lib loaded
	return JNI_VERSION_1_2; //1_2 1_4
}

void JNI_OnUnload(JavaVM *vm, void *reserved)
{
	//native lib unloaded
}

/* Reads LIB version
*/
jstring Java_net_pocketmagic_perseus_BTNative_intReadVersion( JNIEnv* env,jobject thiz )
{
    return (*env)->NewStringUTF(env, "Bluetooth native interface. v1.0 (C)2010 Radu Motisan , radu.motisan@gmail.com");
}

/* Bluetooth easy interface
 */
struct BTDev {
	char name[248];		//dev remote name
	char address[18];	//device address as string
	uint8_t class[3];	//remote cod
	int rssi;
} BTDevs[20];
int BTCount = 0;

jint Java_net_pocketmagic_perseus_BTNative_intSetMode( JNIEnv* env,jobject thiz,int mode)
{
	bdaddr_t 	bdaddr, dev;
	bdaddr_t 	src, dst, dst_first;	
	int 		err, dd;
	char 		addr[18];

	bacpy(&bdaddr, BDADDR_ANY);
	ba2str(&bdaddr, addr);

	int dev_id = hci_devid(addr);
	if (dev_id < 0) {
		dev_id = hci_get_route(NULL);
		hci_devba(dev_id, &src);
	} 
	else
		bacpy(&src, &bdaddr);

	dd = hci_open_dev(dev_id);
	if (dev_id < 0 || dd < 0) {
	        return -1;
	}
	/* Start HCI device */
	//err = ioctl(dd, HCIDEVUP, dev_id) ;

	hci_close_dev(dd);
	return err;
}

/* Discover bluetooth devices and read Report Descriptor
 */
jint Java_net_pocketmagic_perseus_BTNative_intDiscover( JNIEnv* env,jobject thiz, int scantime )
{
	//http://people.csail.mit.edu/albert/bluez-intro/c404.html
	BTCount = 0;
	//---
	uint8_t 	subclass = 0x00;
	bdaddr_t 	bdaddr, dev;
	bdaddr_t 	src, dst, dst_first;	
	uint8_t 	class[3];
	char 		addr[18];
	//-------------		
	bacpy(&bdaddr, BDADDR_ANY);
	ba2str(&bdaddr, addr);
	// link to dev
	int dev_id = hci_devid(addr);
	if (dev_id < 0) {
		dev_id = hci_get_route(NULL);
		hci_devba(dev_id, &src);
	} 
	else
		bacpy(&src, &bdaddr);
	// open socket
	int sock = hci_open_dev(dev_id);
	if (dev_id < 0 || sock < 0) {
	        return -1;
	}
	int length  = scantime; //8
	int flags   = IREQ_CACHE_FLUSH;
	int max_rsp = 255;
	inquiry_info *info;
	info = (inquiry_info*)malloc(max_rsp * sizeof(inquiry_info));
    	// start searching for nearby devices
	int num_rsp = hci_inquiry(dev_id, 
			length, //The inquiry lasts for at most 1.28 * length seconds
			max_rsp, //at most max_rsp devices will be returned
			NULL, 
			&info, 
			flags);
	if (num_rsp <= 0) return num_rsp;

	int i,j, err = -1, found = -1;
	for (i = 0; i < num_rsp; i++) {
		// read remote name
		char name[248] = {0};
		memset(name, 0, sizeof(name));
		if (hci_read_remote_name(sock, &(info+i)->bdaddr, sizeof(name), name, 2000) < 0)
		        strcpy(name, "");
		// read class
		memcpy(class, (info+i)->dev_class, 3);
		// read bt remote address
		bacpy(&dst, &(info+i)->bdaddr);
		ba2str(&dst, addr);
		//if (class[1] == 0x25 && (class[2] == 0x00 || class[2] == 0x01))  //hid device found, do connect!
	
		strcpy(BTDevs[i].address, addr);
		strcpy(BTDevs[i].name, name);
		memcpy(BTDevs[i].class, class, 3);

		BTCount++;
	}
	if (sock >= 0) close( sock );
	bt_free(info);
    //-------------
    return BTCount;
}
/* Read discovered details
 */
jstring Java_net_pocketmagic_perseus_BTNative_intReadDiscoveredName( JNIEnv* env,jobject thiz, jint index )
{
	if (index < BTCount)
		return (*env)->NewStringUTF(env, BTDevs[index].name);
	else 
		return (*env)->NewStringUTF(env, "");
}
jstring Java_net_pocketmagic_perseus_BTNative_intReadDiscoveredAddress( JNIEnv* env,jobject thiz, jint index )
{
	if (index < BTCount)
		return (*env)->NewStringUTF(env, BTDevs[index].address);
	else 
		return (*env)->NewStringUTF(env, "");
}
jbyteArray Java_net_pocketmagic_perseus_BTNative_intReadDiscoveredClass( JNIEnv* env,jobject thiz, jint index )
{
	if (index < BTCount) {
		jbyteArray jb;
		jb=(*env)->NewByteArray(env, 3);
		(*env)->SetByteArrayRegion(env, jb, 0, 3, (jbyte *)BTDevs[index].class);
	    	return (jb);
	}
	else 
		return NULL;
}
/* Serial connection functions
	See BlueZ documentation/source code
*/




