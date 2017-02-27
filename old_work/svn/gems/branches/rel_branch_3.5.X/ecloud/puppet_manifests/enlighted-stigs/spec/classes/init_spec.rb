require 'spec_helper'
describe 'stigs' do

  context 'with defaults for all parameters' do
    it { should contain_class('stigs') }
  end
end
