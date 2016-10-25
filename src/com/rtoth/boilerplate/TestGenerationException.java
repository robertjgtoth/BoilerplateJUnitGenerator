package com.rtoth.boilerplate;

import org.jetbrains.annotations.NotNull;

/**
 * Created by rtoth on 10/23/2016.
 */
public class TestGenerationException extends Exception
{
    public TestGenerationException(@NotNull String message)
    {
        super(message);
    }

    public TestGenerationException(@NotNull String message, @NotNull Throwable cause)
    {
        super(message, cause);
    }
}
