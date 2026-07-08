export type ProjectRole = 'owner' | 'member';

export interface ProjectRequest {
  title: string;
  description?: string;
  memberEmails?: string[];
}

export interface ProjectResponse {
  id: string;
  title: string;
  description?: string;
  ownerId: string;
  role: ProjectRole;
  status: string;
  memberList?: string[];
  createdAt: string;
  updatedAt: string;
}
