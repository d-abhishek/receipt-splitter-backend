
variable "resource_location" {
  type        = string
  default     = "polandcentral"
  description = "Location of the resource group."
}

variable "db_admin_username" {
  type        = string
  description = "PostgreSQL admin username."
}

variable "db_admin_password" {
  type        = string
  sensitive   = true
  description = "PostgreSQL admin password."
}
