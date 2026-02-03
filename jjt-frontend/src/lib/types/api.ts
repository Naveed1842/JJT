export interface ChildDto {
	id: string;
	fullName: string;
	educationAmount: string;
	educationCurrency: string;
}

export interface LedgerEntryDto {
	id: string;
	month: string;
	educationAmount: string;
	educationCurrency: string;
}

export interface LedgerDto {
	childId: string;
	entries: LedgerEntryDto[];
}

export interface ProgressUpdateDto {
	id: string;
	month: string;
	summary: string;
}
