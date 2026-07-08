import { Component, Input, TemplateRef, signal, computed } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';

export interface ColumnConfig<T = any> {
  key: string;
  header: string;
  sortable?: boolean;
  filterable?: boolean;
  render?: (row: T) => string;
}

@Component({
  selector: 'app-data-table',
  imports: [NgTemplateOutlet],
  templateUrl: './data-table.html',
})
export class DataTableComponent<T = any> {
  // Inputs
  @Input() set data(value: T[]) {
    this.rawItems.set(value || []);
  }
  @Input() columns: ColumnConfig<T>[] = [];
  @Input() actionsTemplate?: TemplateRef<{ $implicit: T }>;

  // Signals
  readonly rawItems = signal<T[]>([]);
  readonly sortColumn = signal<string | null>(null);
  readonly sortDirection = signal<'asc' | 'desc' | 'none'>('none');
  readonly columnFilters = signal<{ [key: string]: string }>({});
  readonly hasFilters = computed(() =>
    Object.values(this.columnFilters()).some((v) => v && v.trim() !== '')
  );

  getCellValue(row: any, key: string): any {
    return row[key];
  }

  // Computed data processing: automatically re-filters and re-sorts reactively
  readonly processedData = computed(() => {
    let items = [...this.rawItems()];
    const filters = this.columnFilters();
    const sortBy = this.sortColumn();
    const sortDir = this.sortDirection();

    // 1. Local Filtering
    Object.keys(filters).forEach((key) => {
      const query = filters[key].toLowerCase().trim();
      if (query) {
        items = items.filter((item: any) => {
          const colConfig = this.columns.find((c) => c.key === key);
          const cellValue = colConfig?.render 
            ? colConfig.render(item) 
            : item[key];
          
          return cellValue !== undefined && cellValue !== null
            ? String(cellValue).toLowerCase().includes(query)
            : false;
        });
      }
    });

    // 2. Local Sorting
    if (sortBy && sortDir !== 'none') {
      const colConfig = this.columns.find((c) => c.key === sortBy);
      items.sort((a: any, b: any) => {
        let valA = colConfig?.render ? colConfig.render(a) : a[sortBy];
        let valB = colConfig?.render ? colConfig.render(b) : b[sortBy];

        // Format undefined/null
        valA = valA !== undefined && valA !== null ? valA : '';
        valB = valB !== undefined && valB !== null ? valB : '';

        // Check if numerical comparison is appropriate
        const isNumA = typeof valA === 'number' || (!isNaN(Number(valA)) && !isNaN(parseFloat(valA)));
        const isNumB = typeof valB === 'number' || (!isNaN(Number(valB)) && !isNaN(parseFloat(valB)));

        let comparison = 0;
        if (isNumA && isNumB) {
          comparison = Number(valA) - Number(valB);
        } else {
          comparison = String(valA).localeCompare(String(valB));
        }

        return sortDir === 'asc' ? comparison : -comparison;
      });
    }

    return items;
  });

  // Toggle sorting
  toggleSort(columnKey: string, sortable?: boolean): void {
    if (!sortable) return;

    if (this.sortColumn() !== columnKey) {
      this.sortColumn.set(columnKey);
      this.sortDirection.set('asc');
    } else {
      const currentDir = this.sortDirection();
      if (currentDir === 'asc') {
        this.sortDirection.set('desc');
      } else if (currentDir === 'desc') {
        this.sortDirection.set('none');
        this.sortColumn.set(null);
      } else {
        this.sortDirection.set('asc');
      }
    }
  }

  // Update specific column filter
  updateFilter(columnKey: string, event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.columnFilters.update((prev) => ({
      ...prev,
      [columnKey]: value,
    }));
  }

  // Clear all filters
  clearFilters(): void {
    this.columnFilters.set({});
    this.sortColumn.set(null);
    this.sortDirection.set('none');
  }
}
