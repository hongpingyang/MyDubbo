
package com.hong.py.status;

import com.hong.py.annotation.SPI;

/**
 * StatusChecker
 */
@SPI
public interface StatusChecker {

    /**
     * check status
     *
     * @return status
     */
    Status check();

}