package app.revanced.extension.instagram.misc.followbackindicator;

import java.lang.reflect.Method;
import android.widget.TextView;
import android.view.View;
import java.lang.reflect.Field;
@SuppressWarnings("unused")
public class Helper {

    /**
     * Given method name and class object, this function invokes
     * the method on the object by bypassing access restrictions.
     *
     * @param clsObj The object on which to invoke the method.
     * @param methodName The name of the method to invoke.
     * @return The return value of the invoked method as Object.
     * @throws Exception If an exception occurs during the method invocation.
     **/
    private static Object invokeMethod(Object clsObj, String methodName) throws Exception {
        return clsObj.getClass().getDeclaredMethod(methodName).invoke(clsObj);
    }

    /**
     * Given profile info object, this function return user object.
     *
     * @param classObject profile info object.
     * @return The user data as Object.
     * @throws Exception If an exception occurs during the method invocation.
     **/
    public static Object getViewingProfileUserObject(Object classObject)throws Exception{
        Class<?> clazz = classObject.getClass();
        Field field = clazz.getDeclaredField("FieldName");
        field.setAccessible(true);
        return field.get(classObject);
    }

    /**
     * Given user object, this function returns if an user
     * is following the logged it user or not.
     *
     * @param userObject The viewing profile user object.
     * @return The boolean follow back value.
     * @throws Exception If an exception occurs during the method invocation.
     **/
    public static Boolean getFollowbackInfo(Object userObject) throws Exception {
        Class<?> clazz = Class.forName("className");
        Method method = clazz.getDeclaredMethod("methodName", userObject.getClass());
        method.setAccessible(true);
        Object result = method.invoke(null, userObject);
        return (Boolean) result;
    }


    /**
     * Given user object, this function returns user's id.
     *
     * @param userObject The viewing profile user object.
     * @return The user ID as string.
     * @throws Exception If an exception occurs during the method invocation.
     **/
    public static String getViewingProfileUserId(Object userObject) throws Exception {
        return (String) Helper.invokeMethod(userObject, "getId");
    }

    /**
     * Given badge object and text, this function,
     * sets text to the badge and makes it visible.
     *
     * @param badgeObject The viewing profile user object.
     * @param text String text to set in badge label.
     * @throws Exception If an exception occurs during the method invocation.
     **/
    public static void setInternalBadgeText(Object badgeObject,String text) throws Exception {
        TextView badgeView = (TextView) Helper.invokeMethod(badgeObject,"getView");
        badgeView.setVisibility(View.VISIBLE);
        badgeView.setText(text);
    }
}