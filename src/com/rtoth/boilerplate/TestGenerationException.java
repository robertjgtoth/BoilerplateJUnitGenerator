package com.rtoth.boilerplate;

/**
 * Created by rtoth on 10/23/2016.
 */
public class TestGenerationException extends Exception
{
    public TestGenerationException(String message)
    {
        super(message);
    }

    public TestGenerationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
