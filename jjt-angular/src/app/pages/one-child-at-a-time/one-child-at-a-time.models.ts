import { AvailabilityStatus } from '../../services/sponsor.service';

export interface OneChildViewModel {
  id: string;
  name: string;
  ageText: string;
  city: string;
  tags: string[];
  monthlyCost: string;
  dailyCost?: string;
  storyLine?: string;
  ramadanDonors?: number;
  trustNote?: string;
  coveragePercent?: number;
  status: AvailabilityStatus;
}
