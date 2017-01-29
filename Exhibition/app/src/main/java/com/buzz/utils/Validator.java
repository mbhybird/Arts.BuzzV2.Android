package com.buzz.utils;

/**
 * Created by NickChung on 8/8/16.
 */
public final class Validator {

    private Validator() throws InstantiationException {
        throw new InstantiationException("This class is not for instantiation");
    }

    /**
     * check whether be empty/null or not
     *
     * @param string
     * @return
     */
    public static boolean isEffective(String string) {
        if ((string == null) || ("".equals(string)) || (" ".equals(string))
                || ("null".equals(string)) || ("\n".equals(string)))
            return false;
        else
            return true;
    }
}

