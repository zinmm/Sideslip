## Examples

### Step 1. Add it in your root build.gradle at the end of repositories:

    allprojects {
    	repositories {
    		...
    		maven { url 'https://jitpack.io' }
    	}
    }


### Step 2. Add the dependency

	dependencies {
	        compile 'com.github.zinmm:Sideslip:v0.2'
	}


### Done





### xml

    <com.zin.sideslip.SideslipLayout
        android:id="@+id/sideslipLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:elevation="3dip"
        android:foreground="?android:attr/selectableItemBackground">
    
        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:background="@color/colorAccent"
            android:orientation="horizontal">
    
            <Button
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="LEFT👈☜" />
    
            <Button
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="LEFT" />
        </LinearLayout>
    
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@color/colorPrimary"
            android:orientation="horizontal">
    
            <Button
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="CENTER" />
        </LinearLayout>
    
        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:background="@color/colorPrimaryDark"
            android:orientation="horizontal">
    
            <Button
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="RIGHT" />
    
            <Button
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="RIGHT👉👉" />
    
            <Button
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="👉👉☞" />
        </LinearLayout>
    </com.zin.sideslip.SideslipLayout>



### Java method

setCanLeftRightSwipe(Boolean)  // can left swipe

setCanRightSwipe(Boolean)      // can right swipe

setCanLeftSwipe(Boolean)       // can left right swipe


[！[]（https://jitpack.io/v/zinmm/Sideslip.svg）（https://jitpack.io/#zinmm/Sideslip）