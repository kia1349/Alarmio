package james.alarmio.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import james.alarmio.Alarmio;

public enum PreferenceData {
    THEME(Alarmio.THEME_DAY_NIGHT),
    DAY_AUTO(true),
    DAY_START(6), //hours TODO: change to minutes
    DAY_END(18), //hours
    ALARM_LENGTH(0),
    TIMER_LENGTH(0),
    TIME_ZONES(new String[0]),
    DEFAULT_RINGTONE(null),
    SLEEP_REMINDER(true),
    SLEEP_REMINDER_TIME(420), //minutes
    SLOW_WAKE_UP(true),
    SLOW_WAKE_UP_TIME(5); //minutes

    private String name;
    private Object defaultValue;

    PreferenceData(Object value) {
        name = name();
        defaultValue = value;
    }

    PreferenceData(String name, Object value) {
        this.name = name;
        defaultValue = value;
    }

    public String getName(@Nullable String... args) {
        if (args != null && args.length > 0)
            return String.format(name, (Object[]) args);
        else return name;
    }

    public <T> T getDefaultValue() {
        try {
            return (T) defaultValue;
        } catch (ClassCastException e) {
            throw new TypeMismatchException(this);
        }
    }

    public <T> T getValue(Context context) {
        return getSpecificOverriddenValue(context, (T) getDefaultValue(), (String[]) null);
    }

    public <T> T getValue(Context context, @Nullable T defaultValue) {
        return getSpecificOverriddenValue(context, defaultValue, (String[]) null);
    }

    public <T> T getSpecificValue(Context context, @Nullable String... args) {
        return getSpecificOverriddenValue(context, (T) getDefaultValue(), args);
    }

    public <T> T getSpecificOverriddenValue(Context context, @Nullable T defaultValue, @Nullable String... args) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String name = getName(args);
        T type = defaultValue != null ? defaultValue : (T) getDefaultValue();

        if (type instanceof Object[] && prefs.contains(name + "-length")) {
            try {
                int length = prefs.getInt(name + "-length", 0);

                Object[] array;
                if (type instanceof Boolean[])
                    array = new Boolean[length];
                else if (type instanceof Integer[])
                    array = new Integer[length];
                else if (type instanceof String[])
                    array = new String[length];
                else throw new TypeMismatchException(this);

                for (int i = 0; i < array.length; i++) {
                    if (array instanceof Boolean[])
                        array[i] = prefs.contains(name + "-" + i) ? prefs.getBoolean(name + "-" + i, false) : null;
                    else if (array instanceof Integer[])
                        array[i] = prefs.contains(name + "-" + i) ? prefs.getInt(name + "-" + i, 0) : null;
                    else if (array instanceof String[])
                        array[i] = prefs.getString(name + "-" + i, "");
                    else throw new TypeMismatchException(this);
                }

                return (T) array;
            } catch (ClassCastException e) {
                throw new TypeMismatchException(this, type.getClass());
            }
        } else if (prefs.contains(name)) {
            try {
                if (type instanceof Boolean)
                    return (T) new Boolean(prefs.getBoolean(name, (Boolean) defaultValue));
                else if (type instanceof Integer)
                    return (T) new Integer(prefs.getInt(name, (Integer) defaultValue));
                else if (type instanceof String)
                    return (T) prefs.getString(name, (String) defaultValue);
            } catch (ClassCastException e) {
                throw new TypeMismatchException(this, type.getClass());
            }
        }

        return defaultValue;
    }

    public <T> void setValue(Context context, @Nullable T value) {
        setValue(context, value, (String[]) null);
    }

    public <T> void setValue(Context context, @Nullable T value, @Nullable String... args) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String name = getName(args);

        if (value == null)
            editor.remove(name + (defaultValue instanceof Object[] ? "-length" : ""));
        else if (value instanceof Object[]) {
            Object[] array = (Object[]) value;

            for (int i = 0; i < array.length; i++) {
                Object item = array[i];
                if (item instanceof Boolean)
                    editor.putBoolean(name + "-" + i, (boolean) item);
                else if (item instanceof Integer)
                    editor.putInt(name + "-" + i, (int) item);
                else if (item instanceof String)
                    editor.putString(name + "-" + i, (String) item);
                else throw new TypeMismatchException(this);
            }

            editor.putInt(name + "-length", array.length);
        } else {
            if (value instanceof Boolean)
                editor.putBoolean(name, (Boolean) value);
            else if (value instanceof Integer)
                editor.putInt(name, (Integer) value);
            else if (value instanceof String)
                editor.putString(name, (String) value);
            else throw new TypeMismatchException(this);
        }

        editor.apply();
    }

    public static class TypeMismatchException extends RuntimeException {

        public TypeMismatchException(PreferenceData data) {
            this(data, null);
        }

        public TypeMismatchException(PreferenceData data, Class expectedType) {
            super("Wrong type used for \"" + data.name() + "\""
                    + (data.defaultValue != null ? ": expected " + data.defaultValue.getClass().getName()
                    + (expectedType != null ? ", got " + expectedType.getName() : "") : ""));
        }

    }

}
