export interface CategoryRequest {
  projectId: string;
  name: string;
  description?: string;
  colorCode?: string;
  assignedTo?: string | null;
}

export interface CategoryResponse {
  id: string;
  projectId: string;
  name: string;
  description?: string;
  colorCode?: string;
  assignedTo?: string;
  createdAt: string;
  updatedAt?: string;
}
