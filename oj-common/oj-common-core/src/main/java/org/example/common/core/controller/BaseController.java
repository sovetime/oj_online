package org.example.common.core.controller;

import org.example.common.core.domain.R;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-09
 * Time: 16:58
 */
public class BaseController {

    public R<Void> toR(int rows) {
        return rows>0?R.ok():R.fail();
    }

    public R<Void> toR(boolean result) {
        return result?R.ok():R.fail();
    }
}
