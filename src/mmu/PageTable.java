package mmu;

import mmu.templates.Frame;
import mmu.templates.Page;

import java.util.HashMap;

public class PageTable {
    // HashMap implementation of pagetable
    static HashMap<Page, Frame> pageTable;

    public PageTable() {
        pageTable = new HashMap<>();
    }

    public void addPage(){

    }
}
