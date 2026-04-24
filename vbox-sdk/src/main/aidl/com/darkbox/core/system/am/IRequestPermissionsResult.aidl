// IRequestPermissionsResult.aidl
package com.darkbox.core.system.am;

interface IRequestPermissionsResult {
    boolean onResult(int requestCode,in String[] permissions,in int[] grantResults);
}