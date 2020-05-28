package com.my.finger.data;

import tellh.com.recyclertreeview_lib.LayoutItemType;
import com.my.finger.R;

public class DeptTopItem implements LayoutItemType {
    public String deptName;
    public String deptCd;

    public DeptTopItem(String deptCd, String deptName)
    {
        this.deptCd = deptCd;
        this.deptName = deptName;
    }
    @Override
    public int getLayoutId() {
        return R.layout.item_dir;
    }
}
